package com.pixelsface.towntalk.core.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pixelsface.towntalk.core.data.manager.LocationManagerImpl
import com.pixelsface.towntalk.core.domain.manager.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing location-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * Provides a FusedLocationProviderClient instance.
     *
     * @param context The application context
     * @return A FusedLocationProviderClient instance
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Provides a LocationManager implementation.
     *
     * @param context The application context
     * @param fusedLocationClient The FusedLocationProviderClient instance
     * @return A LocationManager implementation
     */
    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): LocationManager {
        return LocationManagerImpl(context, fusedLocationClient)
    }
} 