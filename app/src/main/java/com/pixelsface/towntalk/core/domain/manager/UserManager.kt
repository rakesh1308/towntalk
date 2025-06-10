package com.pixelsface.towntalk.core.domain.manager

import com.pixelsface.towntalk.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing the current user's state and information.
 */
interface UserManager {
    /**
     * Gets the current user's profile information.
     * @return User object containing the current user's profile data, or null if no user is logged in
     */
    suspend fun getCurrentUser(): User?

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise
     */
    fun isUserLoggedIn(): Boolean

    /**
     * Gets the ID of the currently logged in user.
     * @return String containing the user's ID, or null if no user is logged in
     */
    fun getCurrentUserId(): String?

    /**
     * Updates the user's city.
     * @param city the new city for the user
     */
    suspend fun updateUserCity(city: String)

    /**
     * Updates the user's profile.
     * @param name the new name for the user
     * @param photoUrl the new photo URL for the user, or null if no new photo is provided
     */
    suspend fun updateUserProfile(name: String, photoUrl: String? = null)
} 