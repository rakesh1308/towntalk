package com.pixelsface.towntalk.features.explore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.explore.domain.usecase.SearchPostsUseCase
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.explore.domain.model.Category
import com.pixelsface.towntalk.features.explore.domain.usecase.GetCategoriesUseCase
import com.pixelsface.towntalk.features.explore.domain.usecase.GetTrendingPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define UI State for Explore Screen Search Results
sealed class ExploreSearchUiState {
    object Idle : ExploreSearchUiState() // Initial state before any search
    object Loading : ExploreSearchUiState()
    data class Success(val results: List<Post>) : ExploreSearchUiState()
    object Empty : ExploreSearchUiState() // Search performed, but no results
    data class Error(val message: String) : ExploreSearchUiState()
}

/**
 * ViewModel for the Explore screen.
 */
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchPostsUseCase: SearchPostsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTrendingPostsUseCase: GetTrendingPostsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State for search results
    private val _searchUiState = MutableStateFlow<ExploreSearchUiState>(ExploreSearchUiState.Idle)
    val searchUiState: StateFlow<ExploreSearchUiState> = _searchUiState.asStateFlow()

    // State for categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError.asStateFlow()

    // State for trending posts
    private val _trendingPosts = MutableStateFlow<List<Post>>(emptyList())
    val trendingPosts: StateFlow<List<Post>> = _trendingPosts.asStateFlow()
    private val _trendingPostsLoading = MutableStateFlow(false)
    val trendingPostsLoading: StateFlow<Boolean> = _trendingPostsLoading.asStateFlow()
    private val _trendingPostsError = MutableStateFlow<String?>(null)
    val trendingPostsError: StateFlow<String?> = _trendingPostsError.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCategories()
        loadTrendingPosts() // Load trending posts on init
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().fold(
                onSuccess = { _categories.value = it },
                onFailure = { _categoryError.value = "Failed to load categories: ${it.message}" }
            )
        }
    }

    private fun loadTrendingPosts() {
        viewModelScope.launch {
            _trendingPostsLoading.value = true
            _trendingPostsError.value = null
            getTrendingPostsUseCase().fold(
                onSuccess = { _trendingPosts.value = it },
                onFailure = { _trendingPostsError.value = "Failed to load trending posts: ${it.message}" }
            )
            _trendingPostsLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel() // Cancel previous search job if any
        if (query.isBlank()) {
            _searchUiState.value = ExploreSearchUiState.Idle // Reset to Idle if query is cleared
            // Optionally, reload trending posts if they were hidden during search
            if (_trendingPosts.value.isEmpty() && _trendingPostsError.value == null) loadTrendingPosts()
            return
        }
        if (query.length < 2) {
            _searchUiState.value = ExploreSearchUiState.Idle 
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce: wait for 500ms of inactivity
            performSearch(query)
        }
    }

    fun performSearch(query: String) {
        if (query.length < 2) {
             _searchUiState.value = ExploreSearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchUiState.value = ExploreSearchUiState.Loading
            searchPostsUseCase(query).fold(
                onSuccess = {
                    if (it.isNotEmpty()) {
                        _searchUiState.value = ExploreSearchUiState.Success(it)
                    } else {
                        _searchUiState.value = ExploreSearchUiState.Empty
                    }
                },
                onFailure = { exception ->
                    _searchUiState.value = ExploreSearchUiState.Error("Search failed: ${exception.message}")
                }
            )
        }
    }

    fun onCategorySelected(category: Category) {
        onSearchQueryChanged(category.name)
    }
} 