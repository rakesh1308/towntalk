package com.pixelsface.towntalk.features.chat.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelsface.towntalk.features.chat.ui.chat.ChatScreen
import com.pixelsface.towntalk.features.chat.ui.chatlist.ChatListScreen
import com.pixelsface.towntalk.features.chat.ui.userselection.UserSelectionScreen

const val CHAT_LIST_ROUTE = "chat_list"
const val USER_SELECTION_ROUTE = "user_selection"
const val CHAT_DETAIL_ROUTE_PREFIX = "chat_detail"
const val ARG_CHAT_ID = "chatId"
const val ARG_OTHER_USER_ID = "otherUserId"
const val ARG_OTHER_USER_NAME = "otherUserName"
const val ARG_OTHER_USER_PHOTO_URL = "otherUserPhotoUrl" // Optional, can be passed if readily available
const val ARG_CURRENT_USER_ID = "currentUserId"

// Full route for chat detail, including arguments
const val CHAT_DETAIL_ROUTE = "$CHAT_DETAIL_ROUTE_PREFIX/{$ARG_CHAT_ID}/{$ARG_OTHER_USER_ID}?$ARG_OTHER_USER_NAME={$ARG_OTHER_USER_NAME}&$ARG_OTHER_USER_PHOTO_URL={$ARG_OTHER_USER_PHOTO_URL}"

sealed class ChatScreenRoute(val route: String, val navArguments: List<NamedNavArgument> = emptyList()) {
    object ChatList : ChatScreenRoute(CHAT_LIST_ROUTE)
    object UserSelection : ChatScreenRoute(USER_SELECTION_ROUTE)
    object ChatDetail : ChatScreenRoute(
        route = CHAT_DETAIL_ROUTE,
        navArguments = listOf(
            navArgument(ARG_CHAT_ID) { type = NavType.StringType },
            navArgument(ARG_OTHER_USER_ID) { type = NavType.StringType },
            navArgument(ARG_OTHER_USER_NAME) { type = NavType.StringType; nullable = true },
            navArgument(ARG_OTHER_USER_PHOTO_URL) { type = NavType.StringType; nullable = true }
        )
    ) {
        fun P(chatId: String, otherUserId: String, otherUserName: String?, otherUserPhotoUrl: String?): String {
            var path = "$CHAT_DETAIL_ROUTE_PREFIX/$chatId/$otherUserId"
            val queryParams = mutableListOf<String>()
            otherUserName?.let { queryParams.add("$ARG_OTHER_USER_NAME=$it") }
            otherUserPhotoUrl?.let { queryParams.add("$ARG_OTHER_USER_PHOTO_URL=$it") }
            if (queryParams.isNotEmpty()) {
                path += "?" + queryParams.joinToString("&")
            }
            return path
        }
    }
}

fun NavGraphBuilder.addChatRoutes(
    navController: NavHostController,
    getCurrentUserId: @Composable () -> String?
) {
    composable(ChatScreenRoute.ChatList.route) {
        ChatListScreen(
            onNavigateToChat = { chatId, otherUserId, otherUserName, otherUserPhotoUrl ->
                val safeOtherUserName = otherUserName?.let { java.net.URLEncoder.encode(it, "UTF-8") }
                val photoUrlParam = otherUserPhotoUrl?.let { java.net.URLEncoder.encode(it, "UTF-8")}
                navController.navigate(ChatScreenRoute.ChatDetail.P(chatId, otherUserId, safeOtherUserName, photoUrlParam))
            },
            onNavigateToUserSelection = {
                navController.navigate(ChatScreenRoute.UserSelection.route)
            },
            currentUserId = getCurrentUserId()
        )
    }
    composable(ChatScreenRoute.UserSelection.route) {
        UserSelectionScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToChat = { chatId, otherUserId, otherUserName ->
                val safeOtherUserName = otherUserName?.let { java.net.URLEncoder.encode(it, "UTF-8") }
                navController.navigate(ChatScreenRoute.ChatDetail.P(chatId, otherUserId, safeOtherUserName, null)) {
                    popUpTo(ChatScreenRoute.UserSelection.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    composable(
        route = ChatScreenRoute.ChatDetail.route,
        arguments = ChatScreenRoute.ChatDetail.navArguments
    ) {
        ChatScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
} 