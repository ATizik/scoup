package ru.atizik.scoup.viewmodel

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.CallSuper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import java.io.Serializable
import kotlin.coroutines.CoroutineContext


private const val PARCEL = "__SCOUP_PARCEL"
private const val SERIAL = "__SCOUP_SERIAL"

open class BaseCoordinator(
    private val errorHandler: ErrorHandler,
    final override val coroutineContext: CoroutineContext = SupervisorJob()
) : Disposable, CoroutineScope, DisposableScope, LceModel {

    final override val disposable = CompositeDisposable()
    private val savingListParcelable: MutableList<Pair<()->Parcelable,(Parcelable)->Unit>> = mutableListOf()
    private val savingListSerializable: MutableList<Pair<()->Serializable,(Serializable)->Unit>> = mutableListOf()


    private val lceModel = object : LceModel {
        override val coroutineContext: CoroutineContext = this@BaseCoordinator.coroutineContext
        override val disposable: CompositeDisposable = this@BaseCoordinator.disposable
    }

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun <T : Any> Single<T>.toLce(lce: ConflatedState<Lce<T>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler).toLce(lce)
        }
    }

    override fun <T : Any> Observable<T>.toLce(lce: ConflatedState<Lce<T>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler).toLce(lce)
        }
    }

    override fun Completable.toLce(lce: ConflatedState<Lce<Unit>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler).toLce(lce)
        }
    }

    private inline fun <reified T1:()->T3,T2,reified T3> List<Pair<T1,T2>>.toSavableArray(): Array<T3>? =
        takeIf { it.isNotEmpty() }
            ?.map { it.first() }
            ?.toTypedArray()

    @CallSuper
    open fun onSaveInstanceState (outState: Bundle) {
        savingListParcelable.toSavableArray()
            ?.let{ outState.putParcelableArray(PARCEL,it) }

        savingListSerializable.toSavableArray()
            ?.let { outState.putSerializable(SERIAL,it) }

    }

    @CallSuper
    open fun onRestoreInstanceState (savedInstanceState: Bundle) {
        savedInstanceState.getParcelableArray(PARCEL)
            ?.forEachIndexed { index, parcelable -> savingListParcelable[index].second(parcelable) }
        (savedInstanceState.getSerializable(SERIAL) as? Array<Serializable>)
            ?.forEachIndexed { index, serializable -> savingListSerializable[index].second(serializable) }
    }

    /**
     * For efficiency, saveState function invocations order must be determined at compile time,
     * otherwise you will get ClassCastExceptions. If you can't guarantee order/amount of invocations.
     * you should implement your own persistence logic using [onSaveInstanceState],[onRestoreInstanceState]
     */

    fun <T:Parcelable> saveState(getValue: () -> T, restoreValue: (T) -> Unit) =
        savingListParcelable.add( getValue to (restoreValue as (Parcelable)->Unit))


    fun <T:Serializable> saveStateSerial(getValue: () -> T, restoreValue: (T) -> Unit) =
        savingListSerializable.add( getValue to (restoreValue as (Serializable)->Unit))



    fun <T:Parcelable> ConflatedState<T>.saveState() = apply { saveState( {value}, { parcl -> value = parcl }) }


    fun <T:Serializable> ConflatedState<T>.saveStateSerial() = apply { saveStateSerial( {value}, { serial -> value = serial }) }



    @CallSuper
    override fun dispose() {
        disposable.dispose()
        coroutineContext.cancel()
    }

}

interface ErrorHandler:(Throwable)->Unit