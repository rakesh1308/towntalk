package com.pixelsface.towntalk.features.events.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.ui.theme.TownTalkColors
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    eventId: String,
    onNavigateBack: () -> Unit
) {
    val eventDetailState by viewModel.eventDetailUiState.collectAsState()
    val rsvpOperationState by viewModel.eventOperationUiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(eventId) {
        viewModel.loadEventById(eventId)
        viewModel.resetEventOperationState()
    }

    Scaffold(
        containerColor = TownTalkColors.Background,
        topBar = {
            TopAppBar(
                title = { Text( (eventDetailState as? EventsUiState.Success)?.events?.firstOrNull()?.title ?: "Event Details", style = MaterialTheme.typography.titleLarge, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TownTalkColors.OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TownTalkColors.Primary,
                    titleContentColor = TownTalkColors.OnPrimary,
                    navigationIconContentColor = TownTalkColors.OnPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = eventDetailState) {
                is EventsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TownTalkColors.Primary)
                }
                is EventsUiState.Success -> {
                    val event = state.events.firstOrNull()
                    if (event == null) {
                        Text(
                            "Event not found.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TownTalkColors.OnBackground
                        )
                    } else {
                        EventDetailsContent(event = event, viewModel = viewModel, rsvpState = rsvpOperationState, scrollState = scrollState)
                    }
                }
                is EventsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = TownTalkColors.Error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadEventById(eventId) },
                            colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Primary)
                        ) {
                            Text("Retry", color = TownTalkColors.OnPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                is EventsUiState.Empty -> {
                    Text(
                        "Event details are unavailable.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TownTalkColors.OnBackground
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailsContent(
    event: Event,
    viewModel: EventsViewModel,
    rsvpState: EventOperationUiState,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val currentUserId = viewModel.currentUserId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        if (!event.imageUrls.isNullOrEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                items(event.imageUrls) { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Event Image",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(event.title, style = MaterialTheme.typography.headlineMedium, color = TownTalkColors.OnSurface)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Date & Time", style = MaterialTheme.typography.titleMedium, color = TownTalkColors.PrimaryDark)
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem(icon = Icons.Filled.CalendarToday, label = sdfDate.format(event.startTime.toDate()))
            DetailItem(icon = Icons.Filled.AccessTime, label = "Starts at ${sdfTime.format(event.startTime.toDate())}")
            event.endTime?.let {
                DetailItem(icon = Icons.Filled.TimerOff, label = "Ends at ${sdfTime.format(it.toDate())}")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Location", style = MaterialTheme.typography.titleMedium, color = TownTalkColors.PrimaryDark)
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem(icon = Icons.Filled.LocationCity, label = event.city)
            DetailItem(icon = Icons.Filled.LocationOn, label = event.address)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Details", style = MaterialTheme.typography.titleMedium, color = TownTalkColors.PrimaryDark)
            Spacer(modifier = Modifier.height(8.dp))
            DetailItem(icon = Icons.Filled.Person, label = "Organized by ${event.organizerName}")
            DetailItem(icon = Icons.Filled.Category, label = "Category: ${event.category}")
            DetailItem(icon = Icons.Filled.People, label = "${event.rsvpCount} going")
            Spacer(modifier = Modifier.height(16.dp))
            
            if (event.description.isNotBlank()) {
                Text("About this event", style = MaterialTheme.typography.titleMedium, color = TownTalkColors.PrimaryDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(event.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = TownTalkColors.OnSurface.copy(alpha = 0.85f))
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (currentUserId != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!event.attendees.contains(currentUserId)) {
                        Button(
                            onClick = { viewModel.rsvpToEvent(event.id) },
                            enabled = rsvpState !is EventOperationUiState.Loading,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Primary)
                        ) {
                            val buttonText = if (rsvpState is EventOperationUiState.Loading && viewModel.lastRsvpEventId == event.id) "RSVPing..." else "RSVP to Event"
                            Text(buttonText, style = MaterialTheme.typography.labelLarge, color = TownTalkColors.OnPrimary)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.cancelRsvp(event.id) },
                            enabled = rsvpState !is EventOperationUiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Secondary, contentColor = TownTalkColors.OnSecondary),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            val buttonText = if (rsvpState is EventOperationUiState.Loading && viewModel.lastRsvpEventId == event.id) "Canceling..." else "Cancel RSVP"
                            Text(buttonText, style = MaterialTheme.typography.labelLarge, color = TownTalkColors.OnSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (rsvpState) {
                is EventOperationUiState.Success -> {
                    LaunchedEffect(rsvpState) { 
                         viewModel.loadEventById(event.id)
                    }
                    Text(rsvpState.message, color = TownTalkColors.SecondaryDark, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is EventOperationUiState.Error -> {
                    Text(rsvpState.message, color = TownTalkColors.Error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {} 
            }
            Spacer(modifier = Modifier.height(20.dp)) 
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = TownTalkColors.PrimaryDark
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = TownTalkColors.OnSurface.copy(alpha = 0.9f)
        )
    }
}

// Add this to EventsViewModel to track which event RSVP is for (if needed for loading state)
// private var _lastRsvpEventId: String? = null
// val lastRsvpEventId: String? get() = _lastRsvpEventId
// In rsvpToEvent/cancelRsvp: _lastRsvpEventId = eventId before making the call 