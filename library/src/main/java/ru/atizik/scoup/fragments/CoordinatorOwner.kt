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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.viewmodel.BaseCoordinator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

//TODO:Document
interface CoordinatorOwner<out V : BaseCoordinator>:LateinitFragment {
    val coordinator: V
    val compDisp: CompositeDisposable
    var stateBundle:Bundle

    fun <T> ConflatedState<T>.observe(viewLifecycle: Lifecycle = fragmentDelegate.viewLifecycleOwner.lifecycle, coroutineContext: CoroutineContext = (fragmentDelegate as CoroutineScope).coroutineContext) =
        observe(viewLifecycle,compDisp, coroutineContext)

    fun onRestoreInstanceState(savedInstanceState: Bundle?)

    fun onSaveInstanceState(outState: Bundle)

}

class CoordinatorOwnerImpl<out V : BaseCoordinator>(
    clazz: Class<V>
) : FragmentDelegate(), CoordinatorOwner<V> {
    override val compDisp: CompositeDisposable = CompositeDisposable()
    override lateinit var stateBundle:Bundle


    //FIXME this can be member injected by factory
    override val coordinator: V by lazy(LazyThreadSafetyMode.NONE) {
        fragmentDelegate.getCoordinatorInstance(
            clazz,
            (fragmentDelegate as? ArgumentReceiver<*>)?.argumentClazz,
            fragmentDelegate.getScopeTag()
        ).attachToScope(fragmentDelegate)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let(coordinator::onRestoreInstanceState)
    }

    override fun onStop(owner: LifecycleOwner) {
        fragmentDelegate.fragmentManager!!.saveFragmentInstanceState(fragmentDelegate)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        coordinator.onSaveInstanceState(outState)
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
