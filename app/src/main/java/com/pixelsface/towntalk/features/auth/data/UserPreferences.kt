package com.pixelsface.towntalk.features.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.pixelsface.towntalk.features.auth.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PHOTO_URL, user.photoUrl)
            putString(KEY_USER_CITY, user.city)
            putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            apply()
        }
    }

    fun saveChatBackgroundColor(color: String) {
        prefs.edit().putString(KEY_CHAT_BACKGROUND_COLOR, color).apply()
    }

    fun getChatBackgroundColor(): String {
        return prefs.getString(KEY_CHAT_BACKGROUND_COLOR, "#ECE5DD") ?: "#ECE5DD"
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }

    fun getLastLoginTime(): Long {
        return prefs.getLong(KEY_LAST_LOGIN, 0)
    }

    fun isLoggedIn(): Boolean {
        return prefs.contains(KEY_USER_ID)
    }

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
        private const val KEY_USER_CITY = "user_city"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val KEY_CHAT_BACKGROUND_COLOR = "chat_background_color"
    }
} 