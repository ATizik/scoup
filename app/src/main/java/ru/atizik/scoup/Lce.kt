package ru.atizik.scoup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch

/**
 *  [Lce] - Loading/Content/Error abstraction
 *  sealed hierarchy for modeling view states
 *  Slightly modified:
 *  onContent lambda will be called in any state, if any data is present
 *  Other lambdas are called only in their respective states
 *  [LceModel] maps RxJava's onNext/onSuccess/onComplete events to [Success], onError to [Error];
 *  [Loading] is the default starting state in [ConflatedState]
 */
sealed class Lce<out T> {

    /**
     * data or cache
     */
    open val data: T? = null

    inline fun onContent(f: (T) -> Unit):Lce<T> {
        data?.let(f)
        return this
    }

    data class Success<out T>(override val data: T) : Lce<T>()

    data class Error<out T>(val message: Throwable, override val data: T? = null) : Lce<T>()

    data class Loading <out T>(override val data: T? = null): Lce<T>()
}

fun <T> Lce<T>.orElse(defaultValue: T): T =
    data ?: defaultValue

inline fun <T> Lce<T>.onSuccess(f: T.() -> Unit): Lce<T> {
    if (this is Lce.Success)
        f(this.data)
    return this
}

inline fun <T> Lce<T>.onError(f: Throwable.(cache: T?) -> Unit): Lce<T> {
    if (this is Lce.Error<*>)
        this.message.f(this.data)
    return this
}

inline fun <T> Lce<T>.onLoading(f: T?.() -> Unit): Lce<T> {
    if (this is Lce.Loading<*>)
        f(this.data)
    return this
}


inline fun <T> ReceiveChannel<Lce<T>>.onContent(crossinline f: T.() -> Unit): ReceiveChannel<Lce<T>> =
    map(Dispatchers.Main) {
        it.data?.let(f)
        it
    }


inline fun <T> ReceiveChannel<Lce<T>>.onSuccess(crossinline f: T.() -> Unit): ReceiveChannel<Lce<T>> =
    map(Dispatchers.Main) {
        if (it is Lce.Success)
            f(it.data)
        it
    }


inline fun <T> ReceiveChannel<Lce<T>>.onError(crossinline f: Throwable.(cache: T?) -> Unit): ReceiveChannel<Lce<T>> =
    map(Dispatchers.Main) {
        if (it is Lce.Error<*>)
            it.message.f(it.data)
        it
    }


inline fun <T> ReceiveChannel<Lce<T>>.onLoading(crossinline f: T?.() -> Unit): ReceiveChannel<Lce<T>> =
    map(Dispatchers.Main) {
        if (this is Lce.Loading<*>)
            f(it.data)
        it
    }

//TODO: Document and/or refactor
//This is temporary until full fledged streams are introduced in coroutines
inline fun <T> ReceiveChannel<Lce<T>>.subscribe() = GlobalScope.launch { consumeEach {  } }



val Lce<*>.debug: String
    get() =
        when (this) {
            is Lce.Success -> "S"
            is Lce.Loading -> "L"
            is Lce.Error -> "E"
        }