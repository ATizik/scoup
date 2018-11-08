package ru.atizik.scoup.fragments

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.support.v4.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.viewmodel.BaseCoordinator

//TODO:Document
interface CoordinatorOwner<out V : BaseCoordinator>:LateinitFragment {
    val coordinator: V
    val compDisp: CompositeDisposable

}

class CoordinatorOwnerImpl<out V : BaseCoordinator>(
    clazz: Class<V>
) : FragmentDelegate(), CoordinatorOwner<V> {
    override val compDisp: CompositeDisposable = CompositeDisposable()

    override val coordinator: V by lazy(LazyThreadSafetyMode.NONE) {
        getCoordinatorInstance(
            clazz,
            fragmentDelegate.getScopeTag()
        ).attachToScope(fragmentDelegate)
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
