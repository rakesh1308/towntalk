package com.pixelsface.towntalk.features.profile.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.pixelsface.towntalk.core.domain.model.Post
import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.features.profile.domain.model.Achievement
import com.pixelsface.towntalk.features.profile.domain.model.Activity
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of the ProfileRepository interface.
 */
class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ProfileRepository {

    override fun getCurrentUser(): Flow<ProfileUser> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                        ?: throw IllegalStateException("User data not found")
                    
                    // Get user's achievements
                    firestore.collection("users")
                        .document(userId)
                        .collection("achievements")
                        .get()
                        .addOnSuccessListener { achievementsSnapshot ->
                            val achievements = achievementsSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Achievement::class.java)?.copy(id = doc.id)
                            }
                            
                            // Get user's recent activity
                            firestore.collection("users")
                                .document(userId)
                                .collection("activity")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(10)
                                .get()
                                .addOnSuccessListener { activitySnapshot ->
                                    val activities = activitySnapshot.documents.mapNotNull { doc ->
                                        doc.toObject(Activity::class.java)?.copy(id = doc.id)
                                    }
                                    
                                    // Get user's posts
                                    firestore.collection("posts")
                                        .whereEqualTo("authorId", userId)
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(10)
                                        .get()
                                        .addOnSuccessListener { postsSnapshot ->
                                            val posts = postsSnapshot.documents.mapNotNull { doc ->
                                                doc.toObject(Post::class.java)?.copy(id = doc.id)
                                            }
                                            
                                            val profileUser = ProfileUser(
                                                user = user,
                                                achievements = achievements,
                                                recentActivity = activities,
                                                posts = posts
                                            )
                                            
                                            trySend(profileUser)
                                        }
                                        .addOnFailureListener { e ->
                                            close(e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    close(e)
                                }
                        }
                        .addOnFailureListener { e ->
                            close(e)
                        }
                } else {
                    close(IllegalStateException("User data not found"))
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getUserById(userId: String): Flow<ProfileUser> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                        ?: throw IllegalStateException("User data not found")
                    
                    // Get user's achievements
                    firestore.collection("users")
                        .document(userId)
                        .collection("achievements")
                        .get()
                        .addOnSuccessListener { achievementsSnapshot ->
                            val achievements = achievementsSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Achievement::class.java)?.copy(id = doc.id)
                            }
                            
                            // Get user's recent activity
                            firestore.collection("users")
                                .document(userId)
                                .collection("activity")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(10)
                                .get()
                                .addOnSuccessListener { activitySnapshot ->
                                    val activities = activitySnapshot.documents.mapNotNull { doc ->
                                        doc.toObject(Activity::class.java)?.copy(id = doc.id)
                                    }
                                    
                                    // Get user's posts
                                    firestore.collection("posts")
                                        .whereEqualTo("authorId", userId)
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(10)
                                        .get()
                                        .addOnSuccessListener { postsSnapshot ->
                                            val posts = postsSnapshot.documents.mapNotNull { doc ->
                                                doc.toObject(Post::class.java)?.copy(id = doc.id)
                                            }
                                            
                                            val profileUser = ProfileUser(
                                                user = user,
                                                achievements = achievements,
                                                recentActivity = activities,
                                                posts = posts
                                            )
                                            
                                            trySend(profileUser)
                                        }
                                        .addOnFailureListener { e ->
                                            close(e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    close(e)
                                }
                        }
                        .addOnFailureListener { e ->
                            close(e)
                        }
                } else {
                    close(IllegalStateException("User data not found"))
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getUserProfile(userId: String): Flow<ProfileUser> = getUserById(userId)

    override suspend fun updateProfile(user: User): Flow<User> = callbackFlow {
        try {
            val userId = user.id
            
            // Update user document
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()
            
            trySend(user)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String,
        username: String,
        bio: String,
        phoneNumber: String,
        city: String
    ): Flow<ProfileUser> = callbackFlow {
        try {
            // Get current user data
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                close(IllegalStateException("User not found"))
                return@callbackFlow
            }
            
            val currentUser = userDoc.toObject(User::class.java)?.copy(id = userDoc.id)
                ?: throw IllegalStateException("User data not found")
            
            // Update user with new information
            val updatedUser = currentUser.copy(
                name = name,
                username = username,
                bio = bio,
                phoneNumber = phoneNumber,
                city = city,
                updatedAt = System.currentTimeMillis(),
                // Recalculate profile completion percentage
                profileCompletionPercentage = calculateProfileCompletion(
                    name = name,
                    username = username,
                    bio = bio,
                    phoneNumber = phoneNumber,
                    city = city,
                    photoUrl = currentUser.photoUrl
                )
            )
            
            // Create updates map with all fields
            val updates = mapOf(
                "name" to name,
                "username" to username,
                "bio" to bio,
                "phoneNumber" to phoneNumber,
                "city" to city,
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "profileCompletionPercentage" to updatedUser.profileCompletionPercentage
            )

            // Save updated user
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            // Get and emit updated profile
            val updatedProfile = getUserById(userId).first()
            trySend(updatedProfile)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }

    private fun calculateProfileCompletion(
        name: String,
        username: String,
        bio: String,
        phoneNumber: String,
        city: String,
        photoUrl: String?
    ): Int {
        var completion = 0
        val totalFields = 6
        
        if (name.isNotBlank()) completion++
        if (username.isNotBlank()) completion++
        if (bio.isNotBlank()) completion++
        if (phoneNumber.isNotBlank()) completion++
        if (city.isNotBlank()) completion++
        if (!photoUrl.isNullOrBlank()) completion++
        
        return (completion * 100) / totalFields
    }

    override fun getAchievements(): Flow<List<Achievement>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val achievements = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Achievement::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(achievements)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getUserAchievements(userId: String): Flow<List<Achievement>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val achievements = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Achievement::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(achievements)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getRecentActivity(): Flow<List<Activity>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("activity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Activity::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(activities)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getUserActivity(userId: String): Flow<List<Activity>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("activity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Activity::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(activities)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getPosts(): Flow<List<Post>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        
        val listener = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(posts)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.copy(id = doc.id)
                    }
                    
                    trySend(posts)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun uploadProfilePhoto(photoUri: String): Flow<String> = callbackFlow {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val storageRef = storage.reference.child("profile_photos/$userId.jpg")
            
            val uri = Uri.parse(photoUri)
            val uploadTask = storageRef.putFile(uri)
            
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                // You could emit progress updates here if needed
            }
            
            val downloadUrl = uploadTask.await().storage.downloadUrl.await().toString()
            
            // Update user's photoUrl
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", downloadUrl)
                .await()
            
            trySend(downloadUrl)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun updateUserProfileImage(userId: String, imageUri: Uri): Flow<String> = callbackFlow {
        try {
            val storageRef = storage.reference.child("profile_photos/$userId.jpg")
            
            val uploadTask = storageRef.putFile(imageUri)
            
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                // You could emit progress updates here if needed
            }
            
            val downloadUrl = uploadTask.await().storage.downloadUrl.await().toString()
            
            // Update user's photoUrl
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", downloadUrl)
                .await()
            
            trySend(downloadUrl)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun deleteAccount(): Flow<Boolean> = callbackFlow {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            
            // Delete user's posts
            val postsSnapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            for (doc in postsSnapshot.documents) {
                firestore.collection("posts")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user's achievements
            val achievementsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .await()
            
            for (doc in achievementsSnapshot.documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("achievements")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user's activity
            val activitySnapshot = firestore.collection("users")
                .document(userId)
                .collection("activity")
                .get()
                .await()
            
            for (doc in activitySnapshot.documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("activity")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user document
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            
            // Delete user's profile photo from storage
            val storageRef = storage.reference.child("profile_photos/$userId.jpg")
            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                // Ignore if file doesn't exist
            }
            
            // Delete Firebase Auth account
            auth.currentUser?.delete()?.await()
            
            trySend(true)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun deleteUserAccount(userId: String): Flow<Boolean> = callbackFlow {
        try {
            // Delete user's posts
            val postsSnapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            for (doc in postsSnapshot.documents) {
                firestore.collection("posts")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user's achievements
            val achievementsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .await()
            
            for (doc in achievementsSnapshot.documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("achievements")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user's activity
            val activitySnapshot = firestore.collection("users")
                .document(userId)
                .collection("activity")
                .get()
                .await()
            
            for (doc in activitySnapshot.documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("activity")
                    .document(doc.id)
                    .delete()
                    .await()
            }
            
            // Delete user document
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            
            // Delete user's profile photo from storage
            val storageRef = storage.reference.child("profile_photos/$userId.jpg")
            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                // Ignore if file doesn't exist
            }
            
            trySend(true)
            close()
        } catch (e: Exception) {
            close(e)
        }
    }
} 