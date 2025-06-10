package com.pixelsface.towntalk.core.di

import android.content.Context
import androidx.room.Room
import com.pixelsface.towntalk.core.data.local.TownTalkDatabase
import com.pixelsface.towntalk.core.data.local.dao.CommentDao
import com.pixelsface.towntalk.core.data.local.dao.PostDao
import com.pixelsface.towntalk.core.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TownTalkDatabase {
        return Room.databaseBuilder(
            context,
            TownTalkDatabase::class.java,
            TownTalkDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: TownTalkDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun providePostDao(database: TownTalkDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    @Singleton
    fun provideCommentDao(database: TownTalkDatabase): CommentDao {
        return database.commentDao()
    }
} 