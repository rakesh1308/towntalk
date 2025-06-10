package com.pixelsface.towntalk.features.location.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelsface.towntalk.features.location.presentation.viewmodel.LocationViewModel
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.google.android.gms.maps.model.LatLng

@Composable
fun CityPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String, LatLng) -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchLocations(it)
                    },
                    label = { Text("Search cities") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isEmpty()) {
                    // Show current location if available
                    currentLocation?.let { location ->
                        LocationItemComposable(
                            location = location,
                            onClick = { onLocationSelected(location.city, location.latLng) }
                        )
                    }

                    // Show saved locations
                    if (savedLocations.isNotEmpty()) {
                        Text(
                            text = "Saved Locations",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        savedLocations.forEach { location ->
                            LocationItemComposable(
                                location = location,
                                onClick = { onLocationSelected(location.city, location.latLng) }
                            )
                        }
                    }
                } else {
                    // Show search results
                    LazyColumn {
                        items(searchResults) { location ->
                            LocationItemComposable(
                                location = location,
                                onClick = { onLocationSelected(location.city, location.latLng) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LocationItemComposable(
    location: Location,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = location.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (location.isCurrent) "Current Location" else location.address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
} 