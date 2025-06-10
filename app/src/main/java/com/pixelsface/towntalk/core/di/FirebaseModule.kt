package com.pixelsface.towntalk.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        return try {
            // Try to get existing instance first
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            // If no instance exists, initialize it
            FirebaseApp.initializeApp(context)
            FirebaseApp.getInstance()
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app)
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(app: FirebaseApp): FirebaseStorage = FirebaseStorage.getInstance(app)
} 