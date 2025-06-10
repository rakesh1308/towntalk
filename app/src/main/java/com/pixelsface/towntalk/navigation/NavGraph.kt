package com.pixelsface.towntalk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelsface.towntalk.features.auth.presentation.login.LoginScreen
import com.pixelsface.towntalk.features.auth.presentation.register.RegisterScreen
import com.pixelsface.towntalk.features.chat.ui.addChatRoutes
import com.pixelsface.towntalk.features.events.navigation.EventScreenRoute
import com.pixelsface.towntalk.features.events.ui.CreateEventScreen
import com.pixelsface.towntalk.features.events.ui.EventDetailScreen
import com.pixelsface.towntalk.features.events.ui.EventsScreen
import com.pixelsface.towntalk.features.explore.ui.ExploreScreen
import com.pixelsface.towntalk.features.feed.ui.FeedScreen
import com.pixelsface.towntalk.features.feed.ui.create.CreatePostScreen
import com.pixelsface.towntalk.features.feed.ui.detail.PostDetailScreen
import com.pixelsface.towntalk.features.location.presentation.screens.LocationScreen
import com.pixelsface.towntalk.features.profile.presentation.screens.EditProfileScreen
import com.pixelsface.towntalk.features.profile.presentation.screens.ProfileScreen
import com.pixelsface.towntalk.features.profile.presentation.screens.UserProfileScreen
import com.pixelsface.towntalk.features.location.ui.LocationSelectionScreen
import com.pixelsface.towntalk.features.profile.presentation.viewmodel.UserManagerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreatePost : Screen("create-post")
    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    object Explore : Screen("explore")
    object Profile : Screen("profile")
    object Location : Screen("location")
    object LocationSelection : Screen("location/selection")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            FeedScreen(
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Explore.route)
                },
                onNotificationsClick = {
                    // TODO: Navigate to notifications screen when implemented
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onExploreClick = {
                    navController.navigate(Screen.Explore.route)
                },
                onEventsClick = { city ->
                    navController.navigate(EventScreenRoute.Events.createRoute(city ?: "default_city"))
                },
                onChatClick = {
                    navController.navigate(com.pixelsface.towntalk.features.chat.ui.CHAT_LIST_ROUTE)
                }
            )
        }
        
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPostCreated = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId)) {
                        popUpTo(Screen.CreatePost.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.PostDetail.route) { backStackEntry ->
            PostDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Explore.route) {
            ExploreScreen(
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
               // onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = EventScreenRoute.Events.route,
            arguments = listOf(navArgument("city") { type = NavType.StringType })
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: "pune"
            EventsScreen(
                city = city,
                onNavigateToCreateEvent = {
                    navController.navigate(EventScreenRoute.CreateEvent.route)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(EventScreenRoute.EventDetail.createRoute(eventId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = EventScreenRoute.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            if (eventId != null) {
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(route = EventScreenRoute.CreateEvent.route) {
            CreateEventScreen(
                onNavigateBack = { navController.popBackStack() },
                onEventCreatedSuccessfully = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Profile.route) {
            val userManager = hiltViewModel<UserManagerViewModel>()
            val currentUserId = userManager.getCurrentUserId()
            
            if (currentUserId != null) {
                ProfileScreen(
                    userId = currentUserId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate("profile/edit") },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onLogout = {
                        userManager.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLocationClick = {
                        navController.navigate(Screen.Location.route)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            }
        }
        
        composable("profile/edit") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "profile/user/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                UserProfileScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    }
                )
            }
        }

        composable(Screen.Location.route) {
            LocationScreen(
                onNavigateBack = { navController.popBackStack() },
                onLocationConfirmed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LocationSelection.route) {
            LocationSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onLocationSelected = { location ->
                    navController.popBackStack()
                }
            )
        }

        addChatRoutes(
            navController = navController,
            getCurrentUserId = {
                val userManager = hiltViewModel<UserManagerViewModel>()
                userManager.getCurrentUserId()
            }
        )
    }
} 