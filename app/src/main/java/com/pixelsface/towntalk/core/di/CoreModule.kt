package com.pixelsface.towntalk.core.di

import android.content.Context
import android.content.SharedPreferences
import com.pixelsface.towntalk.core.common.utils.FeatureToggle
import com.pixelsface.towntalk.core.common.utils.SharedPreferencesFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    
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
        @ApplicationContext context: Context
    ): FeatureToggle {
        return SharedPreferencesFeatureToggle.create(context)
    }
} 