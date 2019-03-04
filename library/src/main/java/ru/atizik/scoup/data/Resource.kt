package ru.atizik.scoup.data

/**
 *  Wrapper for resources inside ViewState
 */
sealed class Resource<out T> {

    abstract fun <R> map(f: (T) -> R): Resource<R>

    override fun toString(): String = this.orElse("Res." + this.javaClass.simpleName).toString()

    data class Success<out T>(val data: T) : Resource<T>() {
        override fun <R> map(f: (T) -> R): Resource<R> = Success(f(data))
    }

    data class Error(val message: String) : Resource<Nothing>() {
        constructor(t: Throwable) : this(t.message ?: "")

        override fun <R> map(f: (Nothing) -> R): Resource<R> = this
    }

    object Loading : Resource<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): Resource<R> = this
    }

    object Empty : Resource<Nothing>() {
        override fun <R> map(f: (Nothing) -> R): Resource<R> = this
    }
}

fun <T> Resource<T>.orElse(defaultValue: T): T =
    (this as? Resource.Success)?.data ?: defaultValue

fun <T> Resource<T>.onSuccess(f: (T) -> Unit): Resource<T> {
    if (this is Resource.Success)
        f(this.data)
    return this
}

fun <T> Resource<T>.onError(f: (String) -> Unit): Resource<T> {
    if (this is Resource.Error)
        f(this.message)
    return this
}

fun <T> Resource<T>.onEmpty(f: () -> Unit): Resource<T> {
    if (this === Resource.Empty)
        f()
    return this
}

fun <T> Resource<T>.onLoading(f: () -> Unit): Resource<T> {
    if (this === Resource.Loading)
        f()
    return this
}