package com.pixelsface.towntalk.features.profile.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pixelsface.towntalk.features.profile.data.repository.ProfileRepositoryImpl
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for the profile feature.
 * Provides dependencies for the profile feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    /**
     * Provides the Firebase implementation of the ProfileRepository.
     *
     * @param auth Firebase Auth instance
     * @param firestore Firebase Firestore instance
     * @param storage Firebase Storage instance
     * @return ProfileRepository implementation
     */
    @Provides
    @Singleton
    fun provideProfileRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository {
        return ProfileRepositoryImpl(auth, firestore, storage)
    }
} 