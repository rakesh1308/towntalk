package com.pixelsface.towntalk.core.common.error

/**
 * Base exception class for all TownTalk application exceptions.
 */
open class TownTalkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when there is a network-related error.
 */
class NetworkException(
    message: String = "Network error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is an authentication error.
 */
class AuthenticationException(
    message: String = "Authentication failed",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is an authorization error.
 */
class AuthorizationException(
    message: String = "You don't have permission to perform this action",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when a resource is not found.
 */
class ResourceNotFoundException(
    message: String = "Resource not found",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is a validation error.
 */
class ValidationException(
    message: String = "Validation failed",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is a server error.
 */
class ServerException(
    message: String = "Server error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is a database error.
 */
class DatabaseException(
    message: String = "Database error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is an unknown error.
 */
class UnknownException(
    message: String = "An unknown error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is an unknown error.
 */
class IllegalArgumentException(
    message: String = "An IllegalArgumentException error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)

/**
 * Exception thrown when there is an unknown error.
 */
class DataNotFoundException(
    message: String = "An DataNotFoundException error occurred",
    cause: Throwable? = null
) : TownTalkException(message, cause)