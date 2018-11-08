package ru.atizik.scoup.viewmodel

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancel
import kotlin.coroutines.experimental.CoroutineContext


//TODO: Document
open class BaseCoordinator(override val coroutineContext: CoroutineContext = Job()) : Disposable, CoroutineScope, DisposableScope, LceModel {
    override val disposable = CompositeDisposable()

    override fun isDisposed(): Boolean = disposable.isDisposed

    @CallSuper
    override fun dispose() {
        disposable.dispose()
        coroutineContext.cancel()
    }

}