package com.pixelsface.towntalk.features.feed.di

import com.pixelsface.towntalk.features.feed.data.repository.FirebasePostRepository
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import com.pixelsface.towntalk.features.feed.domain.use_case.CreatePostUseCase
import com.pixelsface.towntalk.features.feed.domain.use_case.DeletePostUseCase
import com.pixelsface.towntalk.features.feed.domain.use_case.PostInteractionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module for feed-related dependencies.
 */
@Module
@InstallIn(ViewModelComponent::class)
object FeedModule {
    @Provides
    fun providePostRepository(repository: FirebasePostRepository): PostRepository {
        return repository
    }
    
    @Provides
    @ViewModelScoped
    fun provideCreatePostUseCase(repository: PostRepository): CreatePostUseCase {
        return CreatePostUseCase(repository)
    }
    
    @Provides
    @ViewModelScoped
    fun provideDeletePostUseCase(repository: PostRepository): DeletePostUseCase {
        return DeletePostUseCase(repository)
    }
    
    @Provides
    @ViewModelScoped
    fun providePostInteractionUseCase(repository: PostRepository): PostInteractionUseCase {
        return PostInteractionUseCase(repository)
    }
} 