package ru.atizik.scoup

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.reflect.KProperty

//TODO: Document
class ConflatedState<T>(value: Lce<T> = Lce.Loading()) : LifecycleObserver,CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()
    private val _value = ConflatedBroadcastChannel(value)

    var value by this

    private operator fun setValue(thisRef: Any, property: KProperty<*>, value: Lce<T>) {
        _value.offer(value)
    }

    private operator fun getValue(thisRef: Any, property: KProperty<*>): Lce<T> = _value.value


    suspend fun observe(owner: LifecycleOwner, compositeDisposable: CompositeDisposable) = suspendCancellableCoroutine<ReceiveChannel<Lce<T>>> {
        lateinit var s: ReceiveChannel<Lce<T>>
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            s = _value.openSubscription()
            it.resume(s)
        }

        owner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (it.isCompleted.not()) {
                        s = _value.openSubscription()
                        it.resume(s)
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    s.cancel()
                    coroutineContext.cancelChildren()
                    compositeDisposable.clear()
                }
            })

    }
}