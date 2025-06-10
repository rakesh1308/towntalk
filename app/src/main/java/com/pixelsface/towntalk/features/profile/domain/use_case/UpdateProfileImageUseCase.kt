package com.pixelsface.towntalk.features.profile.domain.use_case

import android.net.Uri
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating a user's profile image.
 */
class UpdateProfileImageUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to update a user's profile image.
     * @param userId The ID of the user
     * @param imageUri The URI of the new profile image
     * @return Flow of String representing the URL of the updated image
     */
    suspend operator fun invoke(userId: String, imageUri: Uri): Flow<String> {
        return repository.updateUserProfileImage(userId, imageUri)
    }
} 