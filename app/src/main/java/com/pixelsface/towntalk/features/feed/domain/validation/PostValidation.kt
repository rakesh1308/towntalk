package com.pixelsface.towntalk.features.feed.domain.validation

/**
 * Validation rules for post creation.
 */
object PostValidation {
    const val MAX_TITLE_LENGTH = 100
    const val MIN_CONTENT_LENGTH = 10
    const val MAX_CONTENT_LENGTH = 500
    const val MAX_IMAGES = 5
    const val MAX_IMAGE_SIZE_MB = 5
    
    /**
     * Validates a post title.
     * @return ValidationResult with error message if validation fails.
     */
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.length > MAX_TITLE_LENGTH -> 
                ValidationResult.Error("Title must be less than $MAX_TITLE_LENGTH characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates post content.
     * @return ValidationResult with error message if validation fails.
     */
    fun validateContent(content: String): ValidationResult {
        return when {
            content.isBlank() -> 
                ValidationResult.Error("Content cannot be empty")
            content.length < MIN_CONTENT_LENGTH ->
                ValidationResult.Error("Content must be at least $MIN_CONTENT_LENGTH characters")
            content.length > MAX_CONTENT_LENGTH -> 
                ValidationResult.Error("Content must be less than $MAX_CONTENT_LENGTH characters")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates post category.
     * @return ValidationResult with error message if validation fails.
     */
    fun validateCategory(category: String): ValidationResult {
        return when {
            category.isBlank() -> 
                ValidationResult.Error("Please select a category")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates image count.
     * @return ValidationResult with error message if validation fails.
     */
    fun validateImageCount(count: Int): ValidationResult {
        return when {
            count > MAX_IMAGES -> 
                ValidationResult.Error("Maximum $MAX_IMAGES images allowed")
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates image size.
     * @return ValidationResult with error message if validation fails.
     */
    fun validateImageSize(sizeBytes: Long): ValidationResult {
        val sizeMb = sizeBytes / (1024 * 1024)
        return when {
            sizeMb > MAX_IMAGE_SIZE_MB -> 
                ValidationResult.Error("Image size must be less than $MAX_IMAGE_SIZE_MB MB")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Result of a validation check.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val errorMessage: String?
        get() = (this as? Error)?.message
} 