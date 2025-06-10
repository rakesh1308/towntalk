package com.pixelsface.towntalk.features.location.di

import com.pixelsface.towntalk.features.location.domain.usecase.GetCurrentLocationUseCase
import com.pixelsface.towntalk.features.location.domain.usecase.GetLocationsUseCase
import com.pixelsface.towntalk.features.location.domain.usecase.SaveLocationUseCase
import com.pixelsface.towntalk.features.location.domain.usecase.SearchLocationsUseCase
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for location-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    
    /**
     * Provides the GetLocationsUseCase.
     */
    @Provides
    @Singleton
    fun provideGetLocationsUseCase(locationRepository: LocationRepository): GetLocationsUseCase {
        return GetLocationsUseCase(locationRepository)
    }
    
    /**
     * Provides the GetCurrentLocationUseCase.
     */
    @Provides
    @Singleton
    fun provideGetCurrentLocationUseCase(locationRepository: LocationRepository): GetCurrentLocationUseCase {
        return GetCurrentLocationUseCase(locationRepository)
    }
    
    /**
     * Provides the SearchLocationsUseCase.
     */
    @Provides
    @Singleton
    fun provideSearchLocationsUseCase(locationRepository: LocationRepository): SearchLocationsUseCase {
        return SearchLocationsUseCase(locationRepository)
    }
    
    /**
     * Provides the SaveLocationUseCase.
     */
    @Provides
    @Singleton
    fun provideSaveLocationUseCase(locationRepository: LocationRepository): SaveLocationUseCase {
        return SaveLocationUseCase(locationRepository)
    }
} 