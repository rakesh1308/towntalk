package com.pixelsface.towntalk.core.common.error

/**
 * A sealed class representing the result of an operation.
 * Can be either Success with a value of type T or Error with an exception.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: TownTalkException) : Result<T>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun <T> error(exception: TownTalkException) = Error<T>(exception)
        
        fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Throwable) {
            Error(UnknownException(e.message ?: "Unknown error", e))
        }
    }

    fun isSuccess() = this is Success
    fun isError() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> default
    }

    fun exceptionOrNull(): TownTalkException? = when (this) {
        is Success -> null
        is Error -> exception
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(exception)
    }

    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    fun onError(action: (TownTalkException) -> Unit): Result<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }

    /**
     * Applies `onSuccess` if this is a [Success] or `onFailure` if this is an [Error].
     * @param onSuccess the action to apply if this is a [Success].
     * @param onFailure the action to apply if this is an [Error].
     * @return the result of applying either [onSuccess] or [onFailure].
     */
    inline fun <R> fold(
        onSuccess: (value: T) -> R,
        onFailure: (exception: TownTalkException) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onFailure(exception)
    }
} 