package com.pixelsface.towntalk.features.events.domain.usecase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import kotlinx.coroutines.tasks.await

class UploadEventImagesUseCase @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth // Keep auth in case rules need user later, or for consistency
) {
    suspend operator fun invoke(eventId: String, imageUris: List<Uri>): Result<List<String>> = withContext(Dispatchers.IO) {
        if (eventId.isBlank()) {
            return@withContext Result.Error(TownTalkException("Event ID cannot be blank for image upload"))
        }
        val currentUserId = auth.currentUser?.uid // Optional: include if your storage rules use it for events too
        if (currentUserId.isNullOrBlank()) { // Or remove this check if user id is not part of event image path/rules
            // Depending on your security rules for event_images, this might or might not be needed.
            // If rules for event_images only care about eventId, this isn't strictly necessary.
            // However, it's good practice to ensure an authenticated user is performing the action.
            // return@withContext Result.Error(TownTalkException("User not authenticated for event image upload"))
        }

        try {
            val uploadedImageUrls = imageUris.map { imageUri ->
                async /*(Dispatchers.IO)*/ { // Already in IO context from withContext
                    val imageId = UUID.randomUUID().toString()
                    // New path: event_images/{eventId}/{imageId}.jpg
                    // If you want to include userId in path: event_images/{userId}/{eventId}/{imageId}.jpg
                    val imageRef = storage.reference.child("event_images/$eventId/$imageId.jpg")
                    
                    imageRef.putFile(imageUri).await()
                    imageRef.downloadUrl.await().toString()
                }
            }.awaitAll()

            Result.Success(uploadedImageUrls)
        } catch (e: Exception) {
            Result.Error(TownTalkException(e.message ?: "Failed to upload one or more event images", e.cause))
        }
    }
} 