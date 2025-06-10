package com.pixelsface.towntalk.core.domain.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelsface.towntalk.core.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManagerImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserManager {

    override suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        
        return try {
            // This is a synchronous call which might block the main thread
            // In a real app, you might want to use a cached value or a different approach
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (snapshot.exists()) {
                snapshot.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    override suspend fun updateUserCity(city: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")
        
        firestore.collection("users")
            .document(userId)
            .update("city", city)
            .await()
    }
    
    override suspend fun updateUserProfile(name: String, photoUrl: String?) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")
        
        val updates = mutableMapOf<String, Any>("name" to name)
        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }
        
        firestore.collection("users")
            .document(userId)
            .update(updates)
            .await()
    }
    
    // This method is kept for backward compatibility or future use
    fun getCurrentUserFlow(): Flow<User> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")
        
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(user)
                    } else {
                        close(IllegalStateException("User data is null"))
                    }
                } else {
                    close(IllegalStateException("User document does not exist"))
                }
            }

        awaitClose { listener.remove() }
    }
} 