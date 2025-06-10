package com.pixelsface.towntalk.features.explore.di

import com.pixelsface.towntalk.features.explore.data.repository.FirebaseCategoryRepository
import com.pixelsface.towntalk.features.explore.domain.repository.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Or ViewModelComponent if scoped to ViewModels
abstract class ExploreDataModule {

    @Binds
    @Singleton // Or @ViewModelScoped if InstallIn(ViewModelComponent::class)
    abstract fun bindCategoryRepository(
        firebaseCategoryRepository: FirebaseCategoryRepository
    ): CategoryRepository

    // Bind other Explore feature repositories here if any
} 