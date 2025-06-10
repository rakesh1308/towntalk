package com.pixelsface.towntalk.core.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelsface.towntalk.core.common.error.TownTalkException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * A base composable function that provides common UI patterns like loading states,
 * error handling, and event collection.
 *
 * @param state The UI state for the screen
 * @param events Flow of UI events to be handled
 * @param onEvent Handles UI events
 * @param content The main content of the screen
 */
@Composable
fun BaseScreen(
    state: BaseUiState,
    events: Flow<BaseUiEvent>,
    onEvent: (BaseUiEvent) -> Unit,
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is BaseUiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.exception.message ?: "An error occurred",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
                is BaseUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
                is BaseUiEvent.Navigate -> {
                    // Navigation will be handled by the NavHost
                    onEvent(event)
                }
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }
}

/**
 * Base screen composable that provides common UI functionality for all screens.
 *
 * @param title The title to display in the top app bar
 * @param onNavigateBack Callback for when the back button is clicked
 * @param state The UI state for the screen
 * @param events Flow of UI events to handle
 * @param content The main content of the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    title: String,
    onNavigateBack: () -> Unit,
    state: BaseUiState,
    events: Flow<BaseUiEvent>,
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle UI events
    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is BaseUiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.exception.message ?: "An error occurred",
                        duration = SnackbarDuration.Short
                    )
                }
                is BaseUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }

                is BaseUiEvent.Navigate -> TODO()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icons.Default.ArrowBack
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
            
            // Show loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Show error message
            state.error?.let { error ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error?: "An error occurred",
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                }
            }
        }
    }
} 