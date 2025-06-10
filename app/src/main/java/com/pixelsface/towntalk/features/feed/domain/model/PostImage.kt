package com.pixelsface.towntalk.features.feed.domain.model

/**
 * Represents an image in a post with its upload status and metadata.
 */
data class PostImage(
    val id: String,
    val uri: String,
    val size: Long,
    val type: String,
    val uploadStatus: UploadStatus = UploadStatus.Pending,
    val downloadUrl: String? = null,
    val error: String? = null
)

/**
 * Represents the upload status of an image.
 */
sealed class UploadStatus {
    object Pending : UploadStatus()
    object Uploading : UploadStatus()
    object Success : UploadStatus()
    data class Error(val message: String) : UploadStatus()
    
    fun isPending() = this is Pending
    
    fun isUploading() = this is Uploading
    
    fun isSuccess() = this is Success
    
    fun isError() = this is Error
    
    fun getErrorMessage(): String? = if (this is Error) message else null
} 