package com.pixelsface.towntalk.core.di

import com.pixelsface.towntalk.core.domain.manager.UserManagerImpl
import com.pixelsface.towntalk.core.domain.manager.UserManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserManagerModule {

    @Binds
    @Singleton
    abstract fun bindUserManager(
        userManagerImpl: UserManagerImpl
    ): UserManager
} 