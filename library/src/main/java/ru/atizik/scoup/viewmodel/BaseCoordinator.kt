package ru.atizik.scoup.viewmodel

import android.os.Bundle
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
import kotlin.coroutines.CoroutineContext


open class BaseCoordinator(
    private val errorHandler: ErrorHandler,
    final override val coroutineContext: CoroutineContext = SupervisorJob()
) : Disposable, CoroutineScope, DisposableScope, LceModel {

    final override val disposable = CompositeDisposable()

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

    fun onSaveInstanceState (outState: Bundle) {

    }

    fun onRestoreInstanceState (savedInstanceState: Bundle) {

    }

    @CallSuper
    override fun dispose() {
        disposable.dispose()
        coroutineContext.cancel()
    }

}

interface ErrorHandler:(Throwable)->Unit