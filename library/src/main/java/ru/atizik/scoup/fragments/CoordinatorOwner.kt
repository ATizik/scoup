package ru.atizik.scoup.fragments

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.DisposableScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

//TODO:Document
interface CoordinatorOwner<out V : BaseCoordinator>:LateinitFragment {
    val coordinator: V
    val compDisp: CompositeDisposable


    fun <T> ConflatedState<T>.observe(viewLifecycle: Lifecycle = fragmentDelegate.viewLifecycleOwner.lifecycle, coroutineContext: CoroutineContext = (fragmentDelegate as CoroutineScope).coroutineContext) =
        observe(viewLifecycle,compDisp, coroutineContext)



}

class CoordinatorOwnerImpl<out V : BaseCoordinator>(
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
