package ru.atizik.scoup.fragments

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.DisposableScope
import ru.atizik.scoup.viewmodel.MvState
import kotlin.coroutines.CoroutineContext

//TODO:Document
interface CoordinatorOwner<T : MvState, out V : BaseCoordinator<T>>:LateinitFragment {
    val coordinator: V
    val compDisp: CompositeDisposable


    fun <T> ConflatedState<T>.observe(viewLifecycle: Lifecycle = fragmentDelegate.viewLifecycleOwner.lifecycle, coroutineContext: CoroutineContext = (fragmentDelegate as CoroutineScope).coroutineContext) =
        observe(viewLifecycle,compDisp, coroutineContext)

    fun subscribe(subscriber: (T) -> Unit) = coordinator.stateObservable.subscribeLifecycle(fragmentDelegate.viewLifecycleOwner, subscriber)

    fun <C> withState(block: (T) -> C) = block(coordinator.state)

    private fun <T> Observable<T>.subscribeLifecycle(
        lifecycleOwner: LifecycleOwner? = null,
        subscriber: (T) -> Unit
    ): Disposable {
        if (lifecycleOwner == null) {
            return observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber).disposeOnClear()
        }

        val lifecycleAwareObserver = MvRxLifecycleAwareObserver(
            lifecycleOwner,
            alwaysDeliverLastValueWhenUnlocked = true,
            onNext = Consumer<T> { subscriber(it) }
        )
        return observeOn(AndroidSchedulers.mainThread()).subscribeWith(lifecycleAwareObserver).disposeOnClear()
    }

    fun Disposable.disposeOnClear(): Disposable {
        coordinator.disposable.add(this)
        return this
    }
}

class CoordinatorOwnerImpl<T : MvState, out V : BaseCoordinator<T>>(
    clazz: Class<V>
) : FragmentDelegateFull(), CoordinatorOwner<T, V> {
    override val compDisp: CompositeDisposable = CompositeDisposable()


    //FIXME this can be member injected by factory
    override val coordinator: V by lazy(LazyThreadSafetyMode.NONE) {
        fragmentDelegate.getCoordinatorInstance(
            clazz,
            (fragmentDelegate as? ArgumentReceiver<*>)?.argumentClazz,
            fragmentDelegate.getScopeTag()
        ).attachToScope(fragmentDelegate)
    }

    override fun onPreCreated(savedInstanceState: Bundle?) {
        savedInstanceState?.let(coordinator::onRestoreInstanceState)
    }

    /**
     * [onStopped] will trigger [onSaveInstanceState]
     */
    override fun onStopped() {
        fragmentDelegate.fragmentManager?.saveFragmentInstanceState(fragmentDelegate)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        coordinator.onSaveInstanceState(outState)
    }

    //Technically, [onViewDestroyed] and [onDestroyed] don't belong here,
    // but creating separate class for them is overkill for now
    override fun onViewDestroyed() {
        (fragmentDelegate as? DisposableScope)?.let {
            it.coroutineContext.cancelChildren()
            compDisp.clear()
        }
    }

    override fun onDestroyed() {
        (fragmentDelegate as? DisposableScope)?.let {
            it.coroutineContext.cancel()
            compDisp.dispose()
        }
    }
}



fun <T : Disposable> T.attachToScope(fragment: Fragment): T {
    fragment.lifecycle.addObserver(GenericLifecycleObserver { s, e ->
        if (e == Lifecycle.Event.ON_DESTROY && fragment.isRemovingCompat()) {
            dispose()
        }
    })
    return this
}

fun Fragment.isRemovingCompat() = isRemoving || anyParentIsRemoving()

fun Fragment.anyParentIsRemoving(): Boolean = parentFragment?.let { it.anyParentIsRemoving() || it.isRemoving } ?: false
