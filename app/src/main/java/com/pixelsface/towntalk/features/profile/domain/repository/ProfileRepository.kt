package com.pixelsface.towntalk.features.profile.domain.repository

import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.core.domain.model.Post
import com.pixelsface.towntalk.features.profile.domain.model.Achievement
import com.pixelsface.towntalk.features.profile.domain.model.Activity
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import kotlinx.coroutines.flow.Flow
import android.net.Uri

/**
 * Repository interface for profile-related operations.
 */
interface ProfileRepository {
    /**
     * Get the current user's profile.
     * @return Flow of ProfileUser object
     */
    fun getCurrentUser(): Flow<ProfileUser>

    /**
     * Get a user's profile by ID.
     * @param userId The ID of the user to fetch
     * @return Flow of ProfileUser object
     */
    fun getUserById(userId: String): Flow<ProfileUser>

    /**
     * Get a user's profile by ID.
     * @param userId The ID of the user to fetch
     * @return Flow of ProfileUser object
     */
    fun getUserProfile(userId: String): Flow<ProfileUser>

    /**
     * Update the current user's profile.
     * @param user The updated User object
     * @return Flow of the updated User object
     */
    suspend fun updateProfile(user: User): Flow<User>

    /**
     * Update a user's profile with new information.
     * @param userId The ID of the user
     * @param name The new name for the user
     * @param username The new username for the user
     * @param bio The new bio for the user
     * @param phoneNumber The new phone number for the user
     * @param city The new city for the user
     * @return Flow of ProfileUser representing the updated user's profile
     */
    suspend fun updateUserProfile(
        userId: String,
        name: String,
        username: String,
        bio: String,
        phoneNumber: String,
        city: String
    ): Flow<ProfileUser>

    /**
     * Get the current user's achievements.
     * @return Flow of list of Achievement objects
     */
    fun getAchievements(): Flow<List<Achievement>>

    /**
     * Get a user's achievements.
     * @param userId The ID of the user
     * @return Flow of list of Achievement objects
     */
    fun getUserAchievements(userId: String): Flow<List<Achievement>>

    /**
     * Get the current user's recent activity.
     * @return Flow of list of Activity objects
     */
    fun getRecentActivity(): Flow<List<Activity>>

    /**
     * Get a user's activity.
     * @param userId The ID of the user
     * @return Flow of list of Activity objects
     */
    fun getUserActivity(userId: String): Flow<List<Activity>>

    /**
     * Get the current user's posts.
     * @return Flow of list of Post objects
     */
    fun getPosts(): Flow<List<Post>>

    /**
     * Get a user's posts.
     * @param userId The ID of the user
     * @return Flow of list of Post objects
     */
    fun getUserPosts(userId: String): Flow<List<Post>>

    /**
     * Upload a profile photo.
     * @param photoUri The URI of the photo to upload
     * @return Flow of the uploaded photo URL
     */
    suspend fun uploadProfilePhoto(photoUri: String): Flow<String>

    /**
     * Update a user's profile image.
     * @param userId The ID of the user
     * @param imageUri The URI of the new profile image
     * @return Flow of String representing the URL of the updated image
     */
    suspend fun updateUserProfileImage(userId: String, imageUri: Uri): Flow<String>

    /**
     * Delete the current user's account.
     * @return Flow of Boolean indicating success
     */
    suspend fun deleteAccount(): Flow<Boolean>

    /**
     * Delete a user's account.
     * @param userId The ID of the user
     * @return Flow of Boolean indicating whether the deletion was successful
     */
    suspend fun deleteUserAccount(userId: String): Flow<Boolean>
} 