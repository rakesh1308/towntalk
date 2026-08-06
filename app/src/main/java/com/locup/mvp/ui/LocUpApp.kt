package com.locup.mvp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.location.Location
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locup.mvp.classifier.PostClassifierRepository
import com.locup.mvp.data.entity.PostEntity
import com.locup.mvp.model.Cluster
import com.locup.mvp.repo.LandmarkRepository
import com.locup.mvp.repo.LocUpRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocUpApp(
    location: Location?,
    errorBanner: String?,
    repo: LocUpRepository,
    classifier: PostClassifierRepository,
    onRequestLocation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val posts by repo.observePosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by repo.observeSubscriptions().collectAsStateWithLifecycle(initialValue = emptyList())

    var showCompose by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf(FeedFilter.HERE) }

    val currentCluster = location?.let { Cluster.idFor(it.latitude, it.longitude) }
    val currentLabel = location?.let { LandmarkRepository.labelFor(it.latitude, it.longitude) }

    LaunchedEffect(Unit) { repo.seedSampleDataIfEmpty() }

    val visible = remember(posts, filter, currentCluster, subscriptions) {
        when (filter) {
            FeedFilter.ALL -> posts
            FeedFilter.HERE -> if (currentCluster != null) posts.filter { it.clusterId == currentCluster }
                               else posts
            FeedFilter.SUBSCRIBED -> {
                val ids = subscriptions.map { it.clusterId }.toSet()
                if (ids.isEmpty()) emptyList() else posts.filter { it.clusterId in ids }
            }
        }
    }

    val isSubscribedHere = subscriptions.any { it.clusterId == currentCluster }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LocUp") },
                actions = {
                    if (currentCluster != null) {
                        FilledTonalIconButton(onClick = {
                            scope.launch {
                                repo.toggleSubscription(currentCluster, currentLabel ?: currentCluster)
                            }
                        }) {
                            Text(if (isSubscribedHere) "★" else "☆", fontSize = 18.sp)
                        }
                    }
                    AssistChip(
                        onClick = onRequestLocation,
                        label = {
                            Text(
                                if (location == null) "Use my location"
                                else currentLabel ?: "Cluster ${currentCluster!!.removePrefix("c_")}"
                            )
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCompose = true },
                icon = { Text("+") },
                text = { Text("Post") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            errorBanner?.let {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiaryContainer).padding(12.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            ModelBanner(classifier)
            FilterRow(filter, onChange = { filter = it })
            if (visible.isEmpty()) {
                Text(
                    text = when (filter) {
                        FeedFilter.HERE -> "No posts in your area yet. Be the first!"
                        FeedFilter.SUBSCRIBED -> "You haven't subscribed to any area yet. Tap the star on the top bar to follow your current area."
                        FeedFilter.ALL -> "No posts yet."
                    },
                    modifier = Modifier.padding(20.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.id }) { PostCard(it, repo, scope) }
                }
            }
        }

        if (showCompose) {
            ComposeSheet(
                classifier = classifier,
                location = location,
                onDismiss = { showCompose = false },
                onPublish = { text, lat, lon ->
                    scope.launch { repo.publish(text, lat, lon) }
                    showCompose = false
                }
            )
        }
    }
}

private enum class FeedFilter { ALL, HERE, SUBSCRIBED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(selected: FeedFilter, onChange: (FeedFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == FeedFilter.HERE,
            onClick = { onChange(FeedFilter.HERE) },
            label = { Text("Here") }
        )
        FilterChip(
            selected = selected == FeedFilter.SUBSCRIBED,
            onClick = { onChange(FeedFilter.SUBSCRIBED) },
            label = { Text("Subscribed") }
        )
        FilterChip(
            selected = selected == FeedFilter.ALL,
            onClick = { onChange(FeedFilter.ALL) },
            label = { Text("All") }
        )
    }
}

@Composable
private fun ModelBanner(c: PostClassifierRepository) {
    val label = if (c.isRealModelActive) {
        "On-device inference: TensorFlow Lite (word-embedding)"
    } else {
        "On-device inference: keyword fallback (drop locup_text_classifier.tflite in assets/ to enable real TFLite)"
    }
    Surface(
        color = if (c.isRealModelActive) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (c.isRealModelActive) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PostCard(p: PostEntity, repo: LocUpRepository, scope: kotlinx.coroutines.CoroutineScope) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.author, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                CategoryChip(p.category)
                Spacer(Modifier.weight(1f))
                ConfidenceBadge(p)
            }
            Spacer(Modifier.height(6.dp))
            Text(p.text, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍 Near ${p.areaLabel}", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoteButton(
                    icon = Icons.Filled.KeyboardArrowUp,
                    count = p.upCount,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { scope.launch { repo.upvote(p.id) } }
                )
                Spacer(Modifier.width(8.dp))
                VoteButton(
                    icon = Icons.Filled.KeyboardArrowDown,
                    count = p.downCount,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { scope.launch { repo.downvote(p.id) } }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${(p.modelConfidence * 100).toInt()}% on-device",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun VoteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = tint.copy(alpha = 0.08f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("$count", color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ConfidenceBadge(p: PostEntity) {
    val score = p.confidence
    val (bg, fg, label) = when {
        score >= 5 -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Trusted $score")
        score <= -2 -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Disputed $score")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Confidence $score")
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(label, color = fg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp)
    }
}

@Composable
private fun CategoryChip(label: String) {
    val color = when (label) {
        "Emergency" -> MaterialTheme.colorScheme.error
        "Traffic" -> Color(0xFFFFA000)
        "Event" -> Color(0xFF1E88E5)
        "Civic" -> Color(0xFF6D4C41)
        else -> MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(label, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeSheet(
    classifier: PostClassifierRepository,
    location: Location?,
    onDismiss: () -> Unit,
    onPublish: (text: String, lat: Double, lon: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var text by rememberSaveable { mutableStateOf("") }
    val result = remember(text) {
        if (text.isBlank()) null else classifier.classify(text)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text("New post", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val label = location?.let { LandmarkRepository.labelFor(it.latitude, it.longitude) }
            Text(
                if (location != null) "📍 Near $label  ·  posted as \"You\"" +
                        "\nConfidence starts at 0 and grows as neighbours upvote."
                else "📍 Location unavailable — using Bengaluru demo coords.",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What's happening nearby?") },
                minLines = 3
            )
            Spacer(Modifier.height(10.dp))
            result?.let { r ->
                Text("Local inference (${r.modelName})", fontSize = 11.sp, color = Color.Gray)
                r.allScores.forEach { (lbl, score) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(lbl, modifier = Modifier.width(90.dp), fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { score.coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${(score * 100).toInt()}%", fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                enabled = text.isNotBlank(),
                onClick = {
                    val lat = location?.latitude ?: 12.97
                    val lon = location?.longitude ?: 77.59
                    onPublish(text, lat, lon)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Publish (confidence starts at 0)") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
