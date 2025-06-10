package com.pixelsface.towntalk.core.common.feature

interface FeatureToggle {
    /**
     * Checks if a feature is enabled
     * @param feature The feature to check
     * @return true if the feature is enabled, false otherwise
     */
    fun isEnabled(feature: Feature): Boolean
    
    /**
     * Enables or disables a feature
     * @param feature The feature to modify
     * @param enabled true to enable the feature, false to disable it
     */
    fun setEnabled(feature: Feature, enabled: Boolean)
    
    /**
     * Resets a feature to its default state
     * @param feature The feature to reset
     */
    fun resetToDefault(feature: Feature)
    
    /**
     * Resets all features to their default states
     */
    fun resetAllToDefault()
} 