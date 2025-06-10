package com.pixelsface.towntalk.features.explore.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.explore.domain.model.Category
import com.pixelsface.towntalk.features.explore.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        val sampleCategories = listOf(
            Category(id = "1", name = "Food & Drink", iconName = "Restaurant"),
            Category(id = "2", name = "Local Events", iconName = "Event"),
            Category(id = "3", name = "Services", iconName = "Build"),
            Category(id = "4", name = "For Sale", iconName = "Storefront"),
            Category(id = "5", name = "Community", iconName = "People"),
            Category(id = "6", name = "News", iconName = "Feed"),
            Category(id = "7", name = "Alerts", iconName = "Warning")
        )
        // The use case now delegates to the repository
        return categoryRepository.getCategories()?:Result.Success(sampleCategories)
    }
} 