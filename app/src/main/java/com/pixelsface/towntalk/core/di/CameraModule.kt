package com.pixelsface.towntalk.core.di

import android.content.Context
import com.pixelsface.towntalk.core.data.manager.CameraManagerImpl
import com.pixelsface.towntalk.core.domain.manager.CameraManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context
    ): CameraManager {
        return CameraManagerImpl(context)
    }
} 