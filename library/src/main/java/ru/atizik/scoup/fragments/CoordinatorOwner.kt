package ru.atizik.scoup.fragments

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import ru.atizik.scoup.viewmodel.StateCoordinator
import kotlin.coroutines.CoroutineContext

//TODO:Document
interface CoordinatorOwner<out V : BaseCoordinator>:LateinitFragment {
    val coordinator: V
    val compDisp: CompositeDisposable


    fun <T> ConflatedState<T>.observe(viewLifecycle: Lifecycle = fragmentDelegate.viewLifecycleOwner.lifecycle, coroutineContext: CoroutineContext = (fragmentDelegate as CoroutineScope).coroutineContext) =
        observe(viewLifecycle,compDisp, coroutineContext)

    fun <T> Observable<T>.subscribeLifecycle(
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

open class CoordinatorOwnerImpl<out V : BaseCoordinator>(
    clazz: Class<V>
) : FragmentDelegateFull(), CoordinatorOwner<V> {
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



interface CoordinatorOwnerState<S : MvState, out V : StateCoordinator<S>>:CoordinatorOwner<V> {
    fun subscribe(subscriber: (S) -> Unit) = coordinator.stateObservable.subscribeLifecycle(fragmentDelegate.viewLifecycleOwner, subscriber)

    fun <C> withState(block: (S) -> C) = block(coordinator.state)
}

class CoordinatorOwnerStateImpl<S : MvState, out V : StateCoordinator<S>>(
    clazz: Class<V>
) : CoordinatorOwnerState<S, V>, CoordinatorOwnerImpl<V>(clazz)