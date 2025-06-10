package com.pixelsface.towntalk.features.events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.ui.theme.TownTalkColors
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToEventDetails: (eventId: String) -> Unit,
    city: String,
    onNavigateBack: () -> Unit
) {
    val eventsUiState by viewModel.eventsUiState.collectAsState()

    LaunchedEffect(city) {
        viewModel.loadEvents(city)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Create Event", color = TownTalkColors.OnPrimary) },
                icon = { Icon(Icons.Filled.Add, "Create Event Icon", tint = TownTalkColors.OnPrimary) },
                onClick = onNavigateToCreateEvent,
                containerColor = TownTalkColors.Primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = TownTalkColors.Background)
        ) {
            when (val state = eventsUiState) {
                is EventsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = TownTalkColors.Primary)
                }
                is EventsUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Text(
                            text = "No events found in $city.",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TownTalkColors.OnBackground
                        )
                    } else {
                        EventsList(events = state.events, onEventClick = onNavigateToEventDetails)
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
                            onClick = { viewModel.loadEvents(city) },
                            colors = ButtonDefaults.buttonColors(containerColor = TownTalkColors.Primary)
                        ) {
                            Text("Retry", color = TownTalkColors.OnPrimary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                is EventsUiState.Empty -> {
                    Text(
                        text = "No events found in $city. Be the first to create one!",
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
fun EventsList(events: List<Event>, onEventClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events) { event ->
            EventItem(event = event, onClick = { onEventClick(event.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    val sdfDayMonth = SimpleDateFormat("MMM dd", Locale.getDefault()) // Combined Day and Month
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val context = LocalContext.current
    val density = LocalDensity.current

    val imageSizeDp = 100.dp
    val imageSizePx = with(density) { imageSizeDp.roundToPx() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TownTalkColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min) // Ensures Row takes minimum height based on content
        ) {
            // Image Thumbnail
            val firstImageUrl = event.imageUrls?.firstOrNull()
            if (firstImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(firstImageUrl)
                        .crossfade(true)
                        .size(Size(imageSizePx, imageSizePx)) // Specify size for Coil
                        .build(),
                    contentDescription = "Event Thumbnail",
                    modifier = Modifier
                        .size(imageSizeDp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                // Placeholder for when there is no image
                Box(
                    modifier = Modifier
                        .size(imageSizeDp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TownTalkColors.PrimaryLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = "Event Placeholder",
                        tint = TownTalkColors.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                 Spacer(modifier = Modifier.width(12.dp))
            }

            // Details Column
            Column(modifier = Modifier.fillMaxHeight()) { // fillMaxHeight to align content if image is taller
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium, // Slightly smaller title for list item
                    color = TownTalkColors.OnSurface,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                EventDetailRow(
                    icon = Icons.Filled.CalendarToday,
                    text = "${sdfDayMonth.format(event.startTime.toDate())} at ${sdfTime.format(event.startTime.toDate())}"
                )
                Spacer(modifier = Modifier.height(4.dp))
                EventDetailRow(icon = Icons.Filled.LocationOn, text = event.address, maxLines = 1)
                
                Spacer(modifier = Modifier.weight(1f)) // Pushes content below to bottom if space

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.People, contentDescription = "RSVPs", modifier = Modifier.size(14.dp), tint = TownTalkColors.OnSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${event.rsvpCount} going",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TownTalkColors.SecondaryDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", style = MaterialTheme.typography.bodySmall, color = TownTalkColors.OnSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(8.dp))
                     Icon(Icons.Filled.Category, contentDescription = "Category", modifier = Modifier.size(14.dp), tint = TownTalkColors.OnSurface.copy(alpha = 0.7f))
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(event.category, style = MaterialTheme.typography.bodySmall, color = TownTalkColors.OnSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun EventDetailRow(icon: ImageVector, text: String, maxLines: Int = Int.MAX_VALUE) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TownTalkColors.PrimaryDark.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TownTalkColors.OnSurface.copy(alpha = 0.8f),
            maxLines = maxLines
        )
    }
}