package com.pixelsface.towntalk.core.di

import android.content.Context
import android.content.SharedPreferences
import com.pixelsface.towntalk.core.common.feature.FeatureToggle
import com.pixelsface.towntalk.core.common.feature.SharedPreferencesFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("towntalk_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideFeatureToggle(
        sharedPreferencesFeatureToggle: SharedPreferencesFeatureToggle
    ): FeatureToggle {
        return sharedPreferencesFeatureToggle
    }
} 