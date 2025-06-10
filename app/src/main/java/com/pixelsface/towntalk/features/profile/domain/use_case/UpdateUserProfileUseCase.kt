package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating a user's profile.
 * Handles updating all editable profile fields.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to update a user's profile.
     * @param userId The ID of the user
     * @param name The new name for the user
     * @param username The new username for the user
     * @param bio The new bio for the user
     * @param phoneNumber The new phone number for the user
     * @param city The new city for the user
     * @return Flow of ProfileUser representing the updated user's profile
     */
    suspend operator fun invoke(
        userId: String,
        name: String,
        username: String,
        bio: String,
        phoneNumber: String,
        city: String
    ): Flow<ProfileUser> {
        return repository.updateUserProfile(
            userId = userId,
            name = name,
            username = username,
            bio = bio,
            phoneNumber = phoneNumber,
            city = city
        )
    }
}