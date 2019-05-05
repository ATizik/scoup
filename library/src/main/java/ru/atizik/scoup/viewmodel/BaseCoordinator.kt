package ru.atizik.scoup.viewmodel

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
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
import ru.atizik.scoup.SingleEvent
import java.io.Serializable
import java.util.*
import kotlin.coroutines.CoroutineContext


private const val PARCEL = "__SCOUP_PARCEL"
private const val SERIAL = "__SCOUP_SERIAL"


open class StateCoordinator<T : MvState>(
    initialState: T,
    errorHandler: ErrorHandler,
    coroutineContext: CoroutineContext = SupervisorJob(),
    private val debugMode: Boolean = false
) : BaseCoordinator(errorHandler, coroutineContext) {

    private val stateStore: MvCorStateStore<T> = MvCorStateStore(initialState, coroutineContext)

    internal val state: T
        get() = stateStore.state

    internal val stateObservable: Observable<T>
        get() = stateStore.observable

    /**
     * Call this to mutate the current state.
     * A few important notes about the state reducer.
     * 1) It will not be called synchronously or on the same thread. This is for performance and accuracy reasons.
     * 2) Similar to the execute lambda above, the current state is the state receiver  so the `count` in `count + 1` is actually the count
     *    property of the state at the time that the lambda is called
     * 3) In development, MvRx will do checks to make sure that your setState is pure by calling in multiple times. As a result, DO NOT use
     *    mutable variables or properties from outside the lambda or else it may crash.
     */
    protected fun setState(reducer: T.() -> T) {
        if (debugMode) {
            // Must use `set` to ensure the validated state is the same as the actual state used in reducer
            // Do not use `get` since `getState` queue has lower priority and the validated state would be the state after reduced
            stateStore.set {
                val firstState = this.reducer()
                val secondState = this.reducer()
                if (firstState != secondState) throw IllegalArgumentException("Your reducer must be pure!")
                firstState
            }
        } else {
            stateStore.set(reducer)
        }
    }

    /**
     * Access the current ViewModel state. Takes a block of code that will be run after all current pending state
     * updates are processed. The `this` inside of the block is the state.
     */
    protected fun withState(block: (state: T) -> Unit) {
        stateStore.get(block)
    }
}

open class BaseCoordinator(
    private val errorHandler: ErrorHandler,
    override val coroutineContext: CoroutineContext = SupervisorJob()
) : Disposable, CoroutineScope, DisposableScope, LceModel, ErrorHandler by errorHandler {

    final override val disposable = CompositeDisposable()
    private val savingListParcelable: MutableList<Pair<() -> Parcelable, (Parcelable) -> Unit>> = mutableListOf()
    private val savingListSerializable: MutableList<Pair<() -> Serializable, (Serializable) -> Unit>> = mutableListOf()

    private val lceModel = object : LceModel, ErrorHandler by errorHandler {
        override val coroutineContext: CoroutineContext = this@BaseCoordinator.coroutineContext
        override val disposable: CompositeDisposable = this@BaseCoordinator.disposable
    }

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun <T : Any> Single<T>.toLce(lce: ConflatedState<Lce<T>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler::push).toLce(lce)
        }
    }

    override fun <T : Any> Observable<T>.toLce(lce: ConflatedState<Lce<T>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler::push).toLce(lce)
        }
    }

    override fun Completable.toLce(lce: ConflatedState<Lce<Unit>>): Disposable {
        with(lceModel) {
            return doOnError(errorHandler::push).toLce(lce)
        }
    }

    private inline fun <reified T1 : () -> T3, T2, reified T3> List<Pair<T1, T2>>.toSavableArray(): Array<T3>? =
        takeIf { it.isNotEmpty() }
            ?.map { it.first() }
            ?.toTypedArray()

    @CallSuper
    open fun onSaveInstanceState(outState: Bundle) {
        savingListParcelable.toSavableArray()
            ?.let { outState.putParcelableArray(PARCEL, it) }

        savingListSerializable.toSavableArray()
            ?.let { outState.putSerializable(SERIAL, it) }

    }

    @CallSuper
    open fun onRestoreInstanceState(savedInstanceState: Bundle) {
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

    fun <T : Parcelable> saveState(getValue: () -> T, restoreValue: (T) -> Unit) =
        savingListParcelable.add(getValue to (restoreValue as (Parcelable) -> Unit))


    fun <T : Serializable> saveStateSerial(getValue: () -> T, restoreValue: (T) -> Unit) =
        savingListSerializable.add(getValue to (restoreValue as (Serializable) -> Unit))


    fun <T : Parcelable> ConflatedState<T>.saveState() = apply { saveState({ value }, { parcl -> value = parcl }) }


    fun <T : Serializable> ConflatedState<T>.saveStateSerial() =
        apply { saveStateSerial({ value }, { serial -> value = serial }) }


    @CallSuper
    override fun dispose() {
        disposable.dispose()
        coroutineContext.cancel()
    }

}

interface ErrorHandler {
    val errorsEvent: SingleEvent<ArrayDeque<Throwable>>
    fun push(t: Throwable) {
        errorsEvent.value = errorsEvent.value.apply { push(t) }
    }
}