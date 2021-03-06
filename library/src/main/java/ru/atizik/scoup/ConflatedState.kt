package ru.atizik.scoup

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import android.os.Parcelable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import ru.atizik.scoup.viewmodel.BaseCoordinator
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.reflect.KProperty


//TODO: Document
open class ConflatedState<T>(value: T? = null) : LifecycleObserver {
    private val conflatedBroadcastChannel = value?.let { ConflatedBroadcastChannel(it) } ?: ConflatedBroadcastChannel()

    var value by this

    val valueOrNull
        get() = conflatedBroadcastChannel.valueOrNull

    open operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        conflatedBroadcastChannel.offer(value)
    }

    open operator fun getValue(thisRef: Any, property: KProperty<*>): T = conflatedBroadcastChannel.value

    fun openSubscription() = conflatedBroadcastChannel.openSubscription()

    open fun observe(lifecycle: Lifecycle, compositeDisposable: CompositeDisposable, coroutineContext: CoroutineContext): ReceiveChannel<T> {
        var active = with(lifecycle.currentState) {
            isAtLeast(Lifecycle.State.STARTED) && !isAtLeast(Lifecycle.State.DESTROYED)
        }
        val someChannel = conflatedBroadcastChannel.openSubscription().broadcast()
        val receiveChannel = someChannel.openSubscription()
        CoroutineScope(coroutineContext).launch(Dispatchers.Main) {
            var buffer: T? = null

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
                        compositeDisposable.clear()
                        receiveChannel.cancel()
                    }
                }
            })
            conflatedBroadcastChannel.openSubscription().filter { !active }.consumeEach { buffer = it }
        }
        return receiveChannel
    }
}



