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


open class BaseCoordinator(
    private val errorHandler: ErrorHandler,
    final override val coroutineContext: CoroutineContext = SupervisorJob()
) : Disposable, CoroutineScope, DisposableScope, LceModel {

    final override val disposable = CompositeDisposable()
    val savingListParcelable: MutableList<Pair<()->Parcelable,(Parcelable)->Unit>> = mutableListOf()
    val savingListSerializable: MutableList<Pair<()->Serializable,(Serializable)->Unit>> = mutableListOf()


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
            ?.let{ outState.putParcelableArray(this::class.java.toString() + "PARCEL",it) }

        savingListSerializable.toSavableArray()
            ?.let { outState.putSerializable(this::class.java.toString() + "SERIAL",it) }

    }

    @CallSuper
    open fun onRestoreInstanceState (savedInstanceState: Bundle) {
        savedInstanceState.getParcelableArray(this::class.java.toString() + "PARCEL")
            ?.forEachIndexed { index, parcelable -> savingListParcelable[index].second(parcelable) }
        (savedInstanceState.getSerializable(this::class.java.toString() + "SERIAL") as? Array<Serializable>)
            ?.forEachIndexed { index, serializable -> savingListSerializable[index].second(serializable) }
    }

    fun <T:Parcelable> ConflatedState<T>.saveState() = apply{
        savingListParcelable.add( {value} to { parcl -> value = parcl as T })
    }

    fun <T:Serializable> ConflatedState<T>.saveStateSerial() = apply{
        savingListSerializable.add( {value} to { parcl -> value = parcl as T })
    }

    @CallSuper
    override fun dispose() {
        disposable.dispose()
        coroutineContext.cancel()
    }

}

interface ErrorHandler:(Throwable)->Unit