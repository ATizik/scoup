package ru.atizik.scoup.viewmodel

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.SchedulerSupport
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import kotlin.coroutines.CoroutineContext

interface DisposableScope:CoroutineScope {
    val disposable: CompositeDisposable
    override val coroutineContext: CoroutineContext

    fun <E> ReceiveChannel<E>.onEach(function: (E) -> Unit): ReceiveChannel<E> {
        return map(Dispatchers.Main) {
            function(it)
            it
        }
    }

    fun ReceiveChannel<*>.subscribe() = launch { consumeEach {  } }
}

//TODO: Document
interface LceModel:DisposableScope {
    @SchedulerSupport(SchedulerSupport.NONE)
    fun <T : Any> Single<T>.subscribeBy(
        onError: (Throwable) -> Unit = {},
        onSuccess: (T) -> Unit = {}
    ) = subscribe({ onSuccess(it) }, { onError(it) }).addTo(disposable)

    @SchedulerSupport(SchedulerSupport.NONE)
    fun <T : Any> Observable<T>.subscribeBy(
        onError: (Throwable) -> Unit = {},
        onNext: (T) -> Unit = {}
    ) = subscribe({ onNext(it) }, { onError(it) }).addTo(disposable)

    @SchedulerSupport(SchedulerSupport.NONE)
    fun Completable.subscribeBy(
        onError: (Throwable) -> Unit = {},
        onComplete: () -> Unit = {}
    ) = subscribe({ onComplete() }, { onError(it) }).addTo(disposable)

    @SchedulerSupport(SchedulerSupport.NONE)
    fun <T : Any> Single<T>.toLce(lce: ConflatedState<Lce<T>>) =
        subscribeBy(
            onSuccess = { lce.value = Lce.Success(it) },
            onError = { lce.value = Lce.Error(it, lce.valueOrNull?.data) })

    @SchedulerSupport(SchedulerSupport.NONE)
    fun <T : Any> Observable<T>.toLce(lce: ConflatedState<Lce<T>>) =
        subscribeBy(
            onNext = { lce.value = Lce.Success(it) },
            onError = { lce.value = Lce.Error(it, lce.valueOrNull?.data) })

    @SchedulerSupport(SchedulerSupport.NONE)
    fun Completable.toLce(lce: ConflatedState<Lce<Unit>>) =
        subscribeBy(
            onComplete = { lce.value = Lce.Success(Unit) },
            onError = { lce.value = Lce.Error(it, Unit) })

    @SchedulerSupport(SchedulerSupport.NONE)
    fun <T : Any,S:Throwable> Single<T>.mapError(f: (Throwable)->S) =
        onErrorResumeNext { Single.error(f(it)) }

}