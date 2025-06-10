package com.pixelsface.towntalk.core.common.feature

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesFeatureToggle @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : FeatureToggle {
    
    companion object {
        private const val PREFIX = "feature_toggle_"
    }
    
    override fun isEnabled(feature: Feature): Boolean {
        return sharedPreferences.getBoolean(getKey(feature), feature.defaultValue)
    }
    
    override fun setEnabled(feature: Feature, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(getKey(feature), enabled).apply()
    }
    
    override fun resetToDefault(feature: Feature) {
        setEnabled(feature, feature.defaultValue)
    }
    
    override fun resetAllToDefault() {
        val editor = sharedPreferences.edit()
        Feature.values().forEach { feature ->
            editor.putBoolean(getKey(feature), feature.defaultValue)
        }
        editor.apply()
    }
    
    private fun getKey(feature: Feature): String {
        return PREFIX + feature.name
    }
} 