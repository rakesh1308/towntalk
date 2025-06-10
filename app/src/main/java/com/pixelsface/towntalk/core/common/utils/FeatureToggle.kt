package com.pixelsface.towntalk.core.common.utils

import android.content.Context
import android.content.SharedPreferences

enum class Feature {
    NEW_UI,
    SPRING_BOOT_BACKEND,
    OFFLINE_SUPPORT,
    PRIVATE_MESSAGING,
    EVENT_RSVP;

    val defaultValue: Boolean
        get() = when (this) {
            NEW_UI -> false
            SPRING_BOOT_BACKEND -> false
            OFFLINE_SUPPORT -> true
            PRIVATE_MESSAGING -> false
            EVENT_RSVP -> false
        }
}

interface FeatureToggle {
    fun isEnabled(feature: Feature): Boolean
    fun setEnabled(feature: Feature, enabled: Boolean)
}

class SharedPreferencesFeatureToggle(
    private val sharedPreferences: SharedPreferences
) : FeatureToggle {
    override fun isEnabled(feature: Feature): Boolean {
        return sharedPreferences.getBoolean(feature.name, feature.defaultValue)
    }
    
    override fun setEnabled(feature: Feature, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(feature.name, enabled).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "feature_toggles"
        
        fun create(context: Context): SharedPreferencesFeatureToggle {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return SharedPreferencesFeatureToggle(prefs)
        }
    }
} 