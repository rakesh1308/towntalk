package com.pixelsface.towntalk.core.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelsface.towntalk.features.location.data.repository.FirebaseLocationRepository
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core repository module that provides repositories that are used across multiple features.
 * Feature-specific repositories should be provided in their respective feature modules.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds the FirebaseLocationRepository implementation to the LocationRepository interface.
     * This repository is used across multiple features, so it's provided in the core module.
     */
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        firebaseLocationRepository: FirebaseLocationRepository
    ): LocationRepository
} 