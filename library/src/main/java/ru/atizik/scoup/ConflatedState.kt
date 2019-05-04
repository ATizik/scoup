package ru.atizik.scoup

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
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

    open fun observe(lifecycle: Lifecycle, compositeDisposable: CompositeDisposable, coroutineContext: CoroutineContext): Flow<T> {
        var active = with(lifecycle.currentState) {
            isAtLeast(Lifecycle.State.STARTED) && !isAtLeast(Lifecycle.State.DESTROYED)
        }
        val someChannel = conflatedBroadcastChannel.openSubscription().broadcast()


        val receiveChannel = someChannel.asFlow()
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
                    }
                }
            })
            conflatedBroadcastChannel.openSubscription().filter { !active }.consumeEach { buffer = it }
        }
        return receiveChannel
    }
}



