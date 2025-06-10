package com.pixelsface.towntalk.features.chat.di

import com.pixelsface.towntalk.features.chat.data.repository.FirebaseChatRepository
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatDataModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(firebaseChatRepository: FirebaseChatRepository): ChatRepository

    // We might add providers for FirebaseFirestore, FirebaseAuth if not already globally available,
    // but typically those are in a core AppModule.
    // For now, assuming they are provided elsewhere (e.g., AppModule.kt in core/common/di or similar).
} 