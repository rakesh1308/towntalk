package com.pixelsface.towntalk.features.explore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.pixelsface.towntalk.features.explore.domain.model.Category
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.ui.theme.TownTalkColors
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider

/**
 * Explore screen that displays trending posts and categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
    onPostClick: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    val trendingPosts by viewModel.trendingPosts.collectAsState()
    val trendingPostsLoading by viewModel.trendingPostsLoading.collectAsState()
    val trendingPostsError by viewModel.trendingPostsError.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = TownTalkColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Explore", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TownTalkColors.Primary,
                    titleContentColor = TownTalkColors.OnPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                label = { Text("Search posts, events...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TownTalkColors.Primary,
                    unfocusedBorderColor = TownTalkColors.OnSurface.copy(alpha = 0.3f),
                    focusedLabelColor = TownTalkColors.Primary,
                    cursorColor = TownTalkColors.Primary
                )
            )

            if (categories.isNotEmpty()) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(category = category, onClick = { viewModel.onCategorySelected(category) })
                    }
                }
            }

            categoryError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            when (val state = searchUiState) {
                is ExploreSearchUiState.Idle -> {
                    TrendingPostsSection(
                        trendingPosts = trendingPosts,
                        isLoading = trendingPostsLoading,
                        error = trendingPostsError,
                        onPostClick = onPostClick,
                        onRetry = { viewModel.performSearch(searchQuery) }
                    )
                }
                is ExploreSearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ExploreSearchUiState.Success -> {
                    if (state.results.isEmpty()) { 
                        CenteredMessage(message = "No posts found for your query.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal=16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.results) { post ->
                                SearchResultItem(post = post, onPostClick = onPostClick)
                            }
                        }
                    }
                }
                is ExploreSearchUiState.Empty -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CenteredMessage(message = "No posts found for your query.", modifier = Modifier.padding(vertical=16.dp))
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                        TrendingPostsSection(
                            trendingPosts = trendingPosts,
                            isLoading = trendingPostsLoading,
                            error = trendingPostsError,
                            onPostClick = onPostClick,
                            onRetry = { viewModel.performSearch(searchQuery) }
                        )
                    }
                }
                is ExploreSearchUiState.Error -> {
                    CenteredMessage("Search Error: ${state.message}")
                }
            }
        }
    }
}

@Composable
fun ColumnScope.TrendingPostsSection(
    trendingPosts: List<Post>,
    isLoading: Boolean,
    error: String?,
    onPostClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "Trending Posts",
                tint = TownTalkColors.Primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Trending Posts",
                style = MaterialTheme.typography.titleMedium
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                CenteredMessage(message = "Error: $error")
            }
            trendingPosts.isEmpty() -> {
                CenteredMessage(message = "No trending posts available at the moment.")
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(trendingPosts) { post ->
                        SearchResultItem(post = post, onPostClick = onPostClick)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: Category, onClick: () -> Unit) {
    val icon = mapCategoryIcon(category.iconName)
    SuggestionChip(
        onClick = onClick,
        label = { Text(category.name, style = MaterialTheme.typography.labelMedium) },
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = category.name, modifier = Modifier.size(androidx.compose.material3.SuggestionChipDefaults.IconSize)) }
        },
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant,
//            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
//            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = androidx.compose.material3.SuggestionChipDefaults.suggestionChipBorder(
//            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            enabled = true
        )
    )
}

fun mapCategoryIcon(iconName: String?): ImageVector? {
    return when (iconName) {
        "Restaurant" -> Icons.Filled.Restaurant
        "Event" -> Icons.Filled.Event
        "Build" -> Icons.Filled.Build
        "Storefront" -> Icons.Filled.Storefront
        "People" -> Icons.Filled.People
        "Feed" -> Icons.Filled.Feed
        "Warning" -> Icons.Filled.Warning
        else -> null // Or a default icon like Icons.Filled.Label
    }
}

@Composable
fun CenteredMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchResultItem(
    post: Post,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            val firstImageUrl = post.mediaUrls.firstOrNull { it.isNotBlank() }
            if (firstImageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(firstImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post image for ${post.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage, // Or a specific loading icon
                                contentDescription = "Loading Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage,
                                contentDescription = "Error Loading Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = post.title.ifBlank { "Untitled Post" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By: ${post.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (post.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
} 