package com.pixelsface.towntalk.core.common.repository

import com.pixelsface.towntalk.core.common.error.ErrorHandler
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ResourceNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Base repository class that provides common functionality for all repositories.
 */
abstract class BaseRepository {
    /**
     * Executes a block on the IO dispatcher and returns a Result.
     */
    protected suspend fun <T> ioResult(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        ErrorHandler.safeApiCall(block)
    }
    
    /**
     * Executes a block on the IO dispatcher with timeout and returns a Result.
     */
    protected suspend fun <T> ioResultWithTimeout(
        timeoutMs: Long,
        block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        withTimeout(timeoutMs) {
            ErrorHandler.safeApiCall(block)
        }
    }
    
    /**
     * Executes a block on the IO dispatcher and returns a Result.
     * If the block returns null, returns an error with the specified message.
     */
    protected suspend fun <T> ioResultOrError(
        errorMessage: String,
        block: suspend () -> T?
    ): Result<T> = withContext(Dispatchers.IO) {
        ErrorHandler.safeApiCall(block).flatMap { value ->
            if (value != null) {
                Result.success(value)
            } else {
                Result.error<T>(ResourceNotFoundException(errorMessage))
            }
        }
    }
    
    /**
     * Executes a block on the IO dispatcher and returns a Result.
     * If the block returns an empty list, returns an error with the specified message.
     */
    protected suspend fun <T> ioResultOrEmpty(
        errorMessage: String,
        block: suspend () -> List<T>
    ): Result<List<T>> = withContext(Dispatchers.IO) {
        ErrorHandler.safeApiCall(block).flatMap { list ->
            if (list.isNotEmpty()) {
                Result.success(list)
            } else {
                Result.error<List<T>>(ResourceNotFoundException(errorMessage))
            }
        }
    }
} 