package com.pixelsface.towntalk.core.common.ui

/**
 * Base UI state class that provides common state management functionality.
 * This class can be extended by specific UI states to include additional state properties.
 *
 * @param isLoading Whether the UI is in a loading state
 * @param error The error message to display, if any
 * @param isRefreshing Whether the UI is in a refreshing state
 */
data class BaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
) {
    /**
     * Returns a copy of this state with the loading state updated.
     */
    fun withLoading(isLoading: Boolean) = copy(isLoading = isLoading)
    
    /**
     * Returns a copy of this state with the error state updated.
     */
    fun withError(error: String?) = copy(error = error)
    
    /**
     * Returns a copy of this state with the refreshing state updated.
     */
    fun withRefreshing(isRefreshing: Boolean) = copy(isRefreshing = isRefreshing)
    
    /**
     * Returns a copy of this state with the loading and error states reset.
     */
    fun reset() = copy(isLoading = false, error = null, isRefreshing = false)
    
    /**
     * Returns a copy of this state with the refreshing state reset.
     */
    fun resetRefreshing() = copy(isRefreshing = false)
} 