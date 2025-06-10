package com.pixelsface.towntalk.core.data.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager as AndroidLocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pixelsface.towntalk.core.domain.manager.LocationManager
import com.pixelsface.towntalk.features.location.domain.model.Location as AppLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [LocationManager] using Android's location services.
 * 
 * This class provides functionality to:
 * - Check and request location permissions
 * - Get the current location
 * - Start and stop location updates
 * - Check if location services are enabled
 * - Request enabling location services
 */
class LocationManagerImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : LocationManager {

    /**
     * Checks if the app has location permission.
     *
     * @return true if location permission is granted, false otherwise
     */
    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permission from the user.
     * 
     * Note: This method is a placeholder. The actual permission request should be handled
     * by the UI layer using ActivityResultLauncher. This method is included to satisfy
     * the interface contract.
     */
    override fun requestLocationPermission() {
        // This is a placeholder method. The actual permission request should be handled
        // by the UI layer using ActivityResultLauncher. This is because permission
        // requests require an Activity context and user interaction.
        
        // The UI layer should implement this using:
        // val permissionLauncher = registerForActivityResult(
        //     ActivityResultContracts.RequestMultiplePermissions()
        // ) { permissions ->
        //     val allGranted = permissions.entries.all { it.value }
        //     if (allGranted) {
        //         // Permission granted, proceed with location operations
        //     } else {
        //         // Permission denied, show appropriate UI
        //     }
        // }
        // 
        // permissionLauncher.launch(arrayOf(
        //     Manifest.permission.ACCESS_FINE_LOCATION,
        //     Manifest.permission.ACCESS_COARSE_LOCATION
        // ))
    }

    /**
     * Gets the current location of the device.
     *
     * @return Flow emitting the current location
     */
    override fun getCurrentLocation(): Flow<AppLocation> = flow {
        try {
            if (!hasLocationPermission()) {
                throw SecurityException("Location permission not granted")
            }
            
            if (!isLocationEnabled()) {
                throw IllegalStateException("Location services are disabled")
            }
            
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                emit(it.toAppLocation())
            } ?: throw IllegalStateException("Unable to get location")
        } catch (e: Exception) {
            // Re-throw the exception to be handled by the caller
            throw e
        }
    }

    /**
     * Starts receiving location updates.
     *
     * @return Flow emitting location updates
     */
    override fun startLocationUpdates(): Flow<AppLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }
        
        if (!isLocationEnabled()) {
            throw IllegalStateException("Location services are disabled")
        }
        
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
        }

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toAppLocation())
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            throw SecurityException("Location permission not granted", e)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to start location updates", e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Stops receiving location updates.
     * 
     * Note: This method is a placeholder. Location updates are automatically stopped
     * when the Flow returned by startLocationUpdates() is cancelled.
     */
    override fun stopLocationUpdates() {
        // Location updates are automatically stopped when the Flow is cancelled
        // This method is included to satisfy the interface contract
    }

    /**
     * Checks if location services are enabled on the device.
     *
     * @return true if location services are enabled, false otherwise
     */
    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
    }

    /**
     * Requests the user to enable location services.
     * 
     * This method opens the system settings for location services.
     */
    override fun requestEnableLocationServices() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Converts an Android Location object to the app's Location model.
     *
     * @param location The Android Location object
     * @return The app's Location model
     */
    private fun Location.toAppLocation(): AppLocation {
        return AppLocation(
            id = "current",
            name = "Current Location",
            address = "", // This would require Geocoding to get the address
            latitude = latitude,
            longitude = longitude,
            city = "Unknown City", // Default value since we don't have geocoding yet
            pincode = "Unknown Pincode", // Default value since we don't have geocoding yet
            isCurrent = true
        )
    }
} 