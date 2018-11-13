package ru.atizik.scoup

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.reflect.KProperty

//TODO: Document
class ConflatedState<T>(value: Lce<T> = Lce.Loading()) : LifecycleObserver, CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()
    private val conflatedBroadcastChannel = ConflatedBroadcastChannel(value)

    var value by this

    private operator fun setValue(thisRef: Any, property: KProperty<*>, value: Lce<T>) {
        conflatedBroadcastChannel.offer(value)
    }

    private operator fun getValue(thisRef: Any, property: KProperty<*>): Lce<T> = conflatedBroadcastChannel.value

    fun openSubscription() = conflatedBroadcastChannel.openSubscription()

    suspend fun observe(lifecycle: Lifecycle, compositeDisposable: CompositeDisposable) =
        suspendCancellableCoroutine<ReceiveChannel<Lce<T>>> {
            lateinit var receiveChannel: ReceiveChannel<Lce<T>>
            /**If [observe] was called later than start state, this check will open subscription anyway*/
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && !lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
                receiveChannel = conflatedBroadcastChannel.openSubscription()
                it.resume(receiveChannel)
            }

            /**clears subscriptions and closes [receiveChannel]
             * original [conflatedBroadcastChannel] is not closed since it usually belongs to an entity
             * that will outlive [lifecycleOwner]*/
            lifecycle.addObserver(
                object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) {
                        if (it.isCompleted.not()) {
                            receiveChannel = conflatedBroadcastChannel.openSubscription()
                            it.resume(receiveChannel)
                        }
                    }

                    override fun onStop(owner: LifecycleOwner) {
                        receiveChannel.cancel()
                        coroutineContext.cancelChildren()
                        compositeDisposable.clear()
                    }
                })

        }
}
