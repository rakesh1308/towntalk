package com.pixelsface.towntalk.core.common.error

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * Utility class for handling common error scenarios and converting them to TownTalkException.
 */
object ErrorHandler {
    /**
     * Converts various exceptions to TownTalkException.
     */
    fun handleException(throwable: Throwable): TownTalkException {
        return when (throwable) {
            is TownTalkException -> throwable
            is CancellationException -> UnknownException("Operation was cancelled", throwable)
            is TimeoutCancellationException -> NetworkException("Operation timed out", throwable)
            is UnknownHostException -> NetworkException("No internet connection", throwable)
            is HttpException -> handleHttpException(throwable)
            else -> UnknownException(throwable.message ?: "Unknown error", throwable)
        }
    }
    
    /**
     * Handles HTTP exceptions and converts them to appropriate TownTalkException.
     */
    private fun handleHttpException(exception: HttpException): TownTalkException {
        return when (exception.code()) {
            401 -> AuthenticationException("Authentication failed")
            403 -> AuthorizationException("Access denied")
            404 -> ResourceNotFoundException("Resource not found")
            422 -> ValidationException("Invalid input data")
            500, 502, 503, 504 -> ServerException("Server error occurred")
            else -> NetworkException("Network error: ${exception.message()}")
        }
    }
    
    /**
     * Safely executes a block and returns a Result.
     */
    suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Throwable) {
            Result.error<T>(handleException(e))
        }
    }
    
    /**
     * Safely executes a block with timeout and returns a Result.
     */
    suspend fun <T> safeApiCallWithTimeout(
        timeoutMs: Long,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Throwable) {
            Result.error<T>(handleException(e))
        }
    }
} 