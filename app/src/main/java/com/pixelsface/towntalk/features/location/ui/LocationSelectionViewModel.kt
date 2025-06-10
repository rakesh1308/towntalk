package com.pixelsface.towntalk.features.location.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.usecase.GetLocationsUseCase
import com.pixelsface.towntalk.features.location.domain.usecase.SearchLocationsUseCase
import com.pixelsface.towntalk.features.location.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the LocationSelection screen.
 */
sealed class LocationSelectionUiState {
    object Loading : LocationSelectionUiState()
    data class Success(
        val locations: List<Location>,
        val isSearching: Boolean = false
    ) : LocationSelectionUiState()
    data class Error(
        val message: String,
        val locations: List<Location> = emptyList()
    ) : LocationSelectionUiState()
}

sealed class LocationSelectionEvent {
    object NavigateBack : LocationSelectionEvent()
    data class ShowError(val message: String) : LocationSelectionEvent()
    data class LocationSaved(val location: Location) : LocationSelectionEvent()
}

/**
 * ViewModel for the LocationSelection screen.
 */
@HiltViewModel
class LocationSelectionViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val saveLocationUseCase: SaveLocationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LocationSelectionUiState>(LocationSelectionUiState.Loading)
    val uiState: StateFlow<LocationSelectionUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<LocationSelectionEvent>()
    val events = _events.asSharedFlow()
    
    private var searchJob: Job? = null
    private var currentLocations = emptyList<Location>()
    
    init {
        loadLocations()
    }
    
    fun searchLocations(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = LocationSelectionUiState.Success(currentLocations)
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            _uiState.value = LocationSelectionUiState.Success(currentLocations, isSearching = true)
            
            when (val result = searchLocationsUseCase(query)) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val locations = result.data as List<Location>
                    _uiState.value = LocationSelectionUiState.Success(locations)
                }
                is Result.Error<*> -> {
                    _uiState.value = LocationSelectionUiState.Error(
                        message = result.exception.message ?: "Failed to search locations",
                        locations = currentLocations
                    )
                    _events.emit(LocationSelectionEvent.ShowError(result.exception.message ?: "Failed to search locations"))
                }
            }
        }
    }
    
    fun loadLocations() {
        viewModelScope.launch {
            _uiState.value = LocationSelectionUiState.Loading
            
            when (val result = getLocationsUseCase()) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val locations = result.data as List<Location>
                    currentLocations = locations
                    _uiState.value = LocationSelectionUiState.Success(locations)
                }
                is Result.Error<*> -> {
                    _uiState.value = LocationSelectionUiState.Error(
                        message = result.exception.message ?: "Failed to load locations"
                    )
                    _events.emit(LocationSelectionEvent.ShowError(result.exception.message ?: "Failed to load locations"))
                }
            }
        }
    }
    
    fun saveLocation(location: Location) {
        viewModelScope.launch {
            when (val result = saveLocationUseCase(location)) {
                is Result.Success<*> -> {
                    _events.emit(LocationSelectionEvent.LocationSaved(location))
                }
                is Result.Error<*> -> {
                    _events.emit(LocationSelectionEvent.ShowError(result.exception.message ?: "Failed to save location"))
                }
            }
        }
    }
    
    fun onBackClick() {
        viewModelScope.launch {
            _events.emit(LocationSelectionEvent.NavigateBack)
        }
    }
} 