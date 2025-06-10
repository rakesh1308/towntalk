package com.pixelsface.towntalk.features.location.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location as AndroidLocation
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.core.domain.manager.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

data class LocationUiState(
    val isLoading: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<Location>>(emptyList())
    val savedLocations: StateFlow<List<Location>> = _savedLocations.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Location>>(emptyList())
    val searchResults: StateFlow<List<Location>> = _searchResults.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    fun checkLocationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasPermission
        )

        if (hasPermission) {
            getCurrentLocation()
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    val locationInfo = getLocationFromAndroid(location, isCurrent = true)
                    _currentLocation.value = locationInfo
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not get your location. Please try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error getting location: ${e.message}"
                )
            }
        }
    }

    fun confirmLocation(location: Location) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Update user's city in profile
                userManager.updateUserCity(location.city)
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update location: ${e.message}"
                )
            }
        }
    }

    fun searchLocations(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                return@launch
            }

            try {
                val addresses = geocoder.getFromLocationName(query, 5)
                _searchResults.value = addresses?.mapNotNull { address ->
                    if (address.locality != null) {
                        Location(
                            id = "${address.latitude},${address.longitude}",
                            name = address.locality,
                            address = address.getAddressLine(0) ?: "",
                            latitude = address.latitude,
                            longitude = address.longitude,
                            city = address.locality ?: "Unknown City",
                            pincode = address.postalCode ?: "Unknown Pincode"
                        )
                    } else null
                } ?: emptyList()
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    private fun getLocationFromAndroid(location: AndroidLocation, isCurrent: Boolean = false): Location {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            Location(
                id = "${location.latitude},${location.longitude}",
                name = address.locality ?: "Unknown City",
                address = address.getAddressLine(0) ?: "",
                latitude = location.latitude,
                longitude = location.longitude,
                city = address.locality ?: "Unknown City",
                pincode = address.postalCode ?: "Unknown Pincode",
                isCurrent = isCurrent
            )
        } else {
            Location(
                id = "${location.latitude},${location.longitude}",
                name = "Unknown Location",
                address = "",
                latitude = location.latitude,
                longitude = location.longitude,
                city = "Unknown City",
                pincode = "Unknown Pincode",
                isCurrent = isCurrent
            )
        }
    }

    fun setSelectedLocation(location: Location) {
        // TODO: Save selected location to user preferences or repository
    }
} 