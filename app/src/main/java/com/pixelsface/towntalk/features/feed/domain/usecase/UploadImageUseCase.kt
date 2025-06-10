package com.pixelsface.towntalk.features.feed.domain.usecase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException

/**
 * Use case for uploading an image to Firebase Storage.
 */
class UploadImageUseCase @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    /**
     * Uploads an image to Firebase Storage.
     * The image will be stored at "post_images/{userId}/{imageId}.jpg"
     * @param imageUri The URI of the image to upload.
     * @return A Result containing the download URL of the uploaded image or an error.
     */
    suspend operator fun invoke(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId.isNullOrBlank()) {
                return@withContext Result.Error(TownTalkException("User not authenticated for image upload"))
            }

            val imageId = UUID.randomUUID().toString()
            // New path: post_images/{userId}/{imageId}.jpg
            val imageRef = storage.reference.child("post_images/$currentUserId/$imageId.jpg")

            // Upload the image
            imageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()

            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(TownTalkException(e.message ?: "Failed to upload image while creating new path structure", e.cause))
        }
    }
} 