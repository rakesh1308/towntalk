package com.pixelsface.towntalk.features.profile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelsface.towntalk.features.profile.presentation.screens.EditProfileScreen
import com.pixelsface.towntalk.features.profile.presentation.screens.ProfileScreen
import com.pixelsface.towntalk.features.profile.presentation.screens.UserProfileScreen

/**
 * Navigation component for the profile feature.
 * Defines the navigation graph for the profile feature.
 *
 * @param navController NavHostController for navigation
 */
@Composable
fun ProfileNavigation(
    navController: NavHostController,
    userId: String,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onPostClick: (String) -> Unit
) {
    // Instead of creating a new NavHost, we'll directly render the ProfileScreen
    // and handle navigation through the parent NavController
    ProfileScreen(
        userId = userId,
        onNavigateBack = onNavigateBack,
        onEditProfile = onEditProfile,
        onPostClick = onPostClick,
        onLogout = {},
        onLocationClick = {}
    )
}

/**
 * Sealed class representing the destinations in the profile feature.
 */
sealed class ProfileDestination(val route: String) {
    /**
     * Profile screen destination.
     */
    object Profile : ProfileDestination("profile")
    
    /**
     * Edit profile screen destination.
     */
    object EditProfile : ProfileDestination("profile/edit")
    
    /**
     * User profile screen destination.
     */
    object UserProfile : ProfileDestination("profile/user/{userId}") {
        /**
         * Creates the route with the user ID.
         *
         * @param userId The ID of the user whose profile should be displayed
         * @return The route with the user ID
         */
        fun createRoute(userId: String) = "profile/user/$userId"
    }
} 