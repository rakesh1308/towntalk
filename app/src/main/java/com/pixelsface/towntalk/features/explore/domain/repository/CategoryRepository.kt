package com.pixelsface.towntalk.features.explore.domain.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.explore.domain.model.Category

/**
 * Repository interface for category-related operations.
 */
interface CategoryRepository {
    /**
     * Get available categories for the Explore screen.
     * @return A Result containing a list of categories or an error.
     */
    suspend fun getCategories(): Result<List<Category>>
} 