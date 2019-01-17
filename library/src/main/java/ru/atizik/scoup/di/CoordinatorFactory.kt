package ru.atizik.scoup.di

import android.support.v4.app.Fragment
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import ru.atizik.scoup.fragments.ScopeCounter
import ru.atizik.scoup.fragments.attachToScope
import ru.atizik.scoup.fragments.getScopeTag
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.DisposableScope
import toothpick.Toothpick

/**
 * Creates/gets existing instance of a scoped singleton in `obj` scope
 * Module installation is equivalent to @FragmentSingleton annotation
 * Coordinators mimick lifecycle of AAC ViewModel in a following way:
 * Exists as a singleton in Fragment scope, relies on caller's lifecycle to call Dispose callback(similar to onCleared)
 */


/**
 * Binds lifecycle of fragment to Coordinator just as AAC ViewModel binds to Fragment
 */
inline fun <reified T : BaseCoordinator> bindCoordinatorInstance(obj: Fragment): T =
    getCoordinatorInstance(T::class.java, obj)
        .attachToScope(obj)

/**
 * This coordinator should be disposed manually
 */
@CheckReturnValue
inline fun <reified T : BaseCoordinator> getCoordinatorInstance(tag: Any): T =
    getCoordinatorInstance(T::class.java, tag)

/**
 * This coordinator should be disposed manually
 */
@CheckReturnValue
fun <T : BaseCoordinator> getCoordinatorInstance(coordinatorClass: Class<T>, tag: Any): T =
    Toothpick.openScope(tag)
        .applyModules(ViewModelModule(coordinatorClass))
        .getInstance(coordinatorClass)

/**
 * This coordinator should be disposed manually
 */
@CheckReturnValue
inline fun <reified T : BaseCoordinator> DisposableScope.getFlowCoordinatorInstance(vararg tag: Any): T =
    with(Toothpick.openScopes(*tag).applyModules(ViewModelModule(T::class.java))) {
        val counter = getInstance(ScopeCounter::class.java)
        counter.set.incrementAndGet()
        counter.addTo(this@getFlowCoordinatorInstance.disposable)
        getInstance(T::class.java)
            .also { counter.disposables.add(it) }
    }



/**
 * Doesn't bind lifecycle to this component, just gets a reference to an existing Coordinator
 */
inline fun <reified T : Disposable> getParentCoordinator(parent: Fragment): T =
    Toothpick
        .openScope(parent.getScopeTag())
        .getInstance(T::class.java)

