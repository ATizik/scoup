package ru.atizik.scoup

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.reflect.KProperty

//TODO: Document
class ConflatedState<T>(value: T? = null) : LifecycleObserver, CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()
    private val conflatedBroadcastChannel = value?.let { ConflatedBroadcastChannel(it) } ?: ConflatedBroadcastChannel()

    var value by this

    val valueOrNull
        get() = conflatedBroadcastChannel.valueOrNull

    private operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        conflatedBroadcastChannel.offer(value)
    }

    private operator fun getValue(thisRef: Any, property: KProperty<*>): T = conflatedBroadcastChannel.value

    fun openSubscription() = conflatedBroadcastChannel.openSubscription()

    fun observe(lifecycle: Lifecycle, compositeDisposable: CompositeDisposable): ReceiveChannel<T> {
        var active = with(lifecycle.currentState) {
            isAtLeast(Lifecycle.State.STARTED) && !isAtLeast(Lifecycle.State.DESTROYED)
        }
        val someChannel = conflatedBroadcastChannel.openSubscription().broadcast()
        val receiveChannel = someChannel.openSubscription().dropWhile { !active }

        launch {
            var buffer: T? = null
            conflatedBroadcastChannel.openSubscription().filter { !active }.consumeEach { buffer = it }

            /**clears subscriptions and closes [receiveChannel]
             * original [conflatedBroadcastChannel] is not closed since it usually belongs to an entity
             * that will outlive [lifecycleOwner]*/

            lifecycle.addObserver(GenericLifecycleObserver { s, e ->
                when (e) {
                    Lifecycle.Event.ON_START -> {
                        active = true
                        someChannel.offer(buffer ?: return@GenericLifecycleObserver)
                        buffer = null
                    }
                    Lifecycle.Event.ON_STOP -> {
                        active = false
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        coroutineContext.cancelChildren()
                        compositeDisposable.dispose()
                        receiveChannel.cancel()
                    }
                }
            })
        }
        return receiveChannel
    }
}
