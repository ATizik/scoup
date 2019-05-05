package ru.atizik.scoup

import androidx.lifecycle.Lifecycle
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

class SingleEvent<T>():ConflatedState<T>() {

    private val pending = AtomicBoolean(false)

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        pending.set(true)
        super.setValue(thisRef, property, value)
    }

    override fun observe(
        lifecycle: Lifecycle,
        compositeDisposable: CompositeDisposable,
        coroutineContext: CoroutineContext
    ): ReceiveChannel<T> = super.observe(lifecycle, compositeDisposable, coroutineContext).filter(context = coroutineContext) { pending.compareAndSet(true,false) }

}