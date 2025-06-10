package com.pixelsface.towntalk.features.profile.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pixelsface.towntalk.core.domain.model.Post
import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.features.profile.domain.model.Achievement
import com.pixelsface.towntalk.features.profile.domain.model.Activity
import com.pixelsface.towntalk.features.profile.domain.model.ActivityType
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override fun getCurrentUser(): Flow<ProfileUser> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val user = User(
                id = currentUser.uid,
                name = userDoc.getString("name") ?: currentUser.displayName ?: "",
                username = userDoc.getString("username") ?: "",
                email = currentUser.email ?: "",
                phoneNumber = userDoc.getString("phoneNumber"),
                photoUrl = currentUser.photoUrl?.toString(),
                bio = userDoc.getString("bio") ?: "",
                city = userDoc.getString("city") ?: "",
                joinDate = userDoc.getTimestamp("joinDate")?.toDate()?.time
                    ?: System.currentTimeMillis(),
                postCount = userDoc.getLong("postCount")?.toInt() ?: 0,
                likeCount = userDoc.getLong("likeCount")?.toInt() ?: 0,
                commentCount = userDoc.getLong("commentCount")?.toInt() ?: 0,
                profileCompletionPercentage = userDoc.getLong("profileCompletionPercentage")
                    ?.toInt() ?: 0,
                createdAt = userDoc.getTimestamp("createdAt")?.toDate()?.time
                    ?: System.currentTimeMillis(),
                updatedAt = userDoc.getTimestamp("updatedAt")?.toDate()?.time
                    ?: System.currentTimeMillis()
            )

            val achievements = getAchievements().collect { achievementsList ->
                val activity = getRecentActivity().collect { activityList ->
                    val posts = getPosts().collect { postsList ->
                        emit(
                            ProfileUser(
                                user = user,
                                achievements = achievementsList,
                                recentActivity = activityList,
                                posts = postsList
                            )
                        )
                    }
                }
            }
        } else {
            throw Exception("User not authenticated")
        }
    }

    override fun getUserById(userId: String): Flow<ProfileUser> = flow {
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        if (userDoc.exists()) {
            val user = User(
                id = userId,
                name = userDoc.getString("name") ?: "",
                username = userDoc.getString("username") ?: "",
                email = userDoc.getString("email") ?: "",
                phoneNumber = userDoc.getString("phoneNumber"),
                photoUrl = userDoc.getString("photoUrl"),
                bio = userDoc.getString("bio") ?: "",
                city = userDoc.getString("city") ?: "",
                joinDate = userDoc.getTimestamp("joinDate")?.toDate()?.time
                    ?: System.currentTimeMillis(),
                postCount = userDoc.getLong("postCount")?.toInt() ?: 0,
                likeCount = userDoc.getLong("likeCount")?.toInt() ?: 0,
                commentCount = userDoc.getLong("commentCount")?.toInt() ?: 0,
                profileCompletionPercentage = userDoc.getLong("profileCompletionPercentage")
                    ?.toInt() ?: 0,
                createdAt = userDoc.getTimestamp("createdAt")?.toDate()?.time
                    ?: System.currentTimeMillis(),
                updatedAt = userDoc.getTimestamp("updatedAt")?.toDate()?.time
                    ?: System.currentTimeMillis()
            )

            val achievements = getUserAchievements(userId).collect { achievementsList ->
                val activity = getUserActivity(userId).collect { activityList ->
                    val posts = getUserPosts(userId).collect { postsList ->
                        emit(
                            ProfileUser(
                                user = user,
                                achievements = achievementsList,
                                recentActivity = activityList,
                                posts = postsList
                            )
                        )
                    }
                }
            }
        } else {
            throw Exception("User not found")
        }
    }

    override fun getUserProfile(userId: String): Flow<ProfileUser> = getUserById(userId)

    override suspend fun updateProfile(user: User): Flow<User> = flow {
        val userUpdates = mapOf(
            "name" to user.name,
            "bio" to user.bio,
            "city" to user.city,
            "updatedAt" to System.currentTimeMillis()
        ) as Map<String, Any>

        firestore.collection("users")
            .document(user.id)
            .update(userUpdates)
            .await()

        emit(user)
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String,
        username: String,
        bio: String,
        phoneNumber: String,
        city: String,
    ): Flow<ProfileUser> = flow {
        try {
            // First check if user document exists
            val userDoc = firestore.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                // Create a new user document with default values
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val currentTime = com.google.firebase.Timestamp.now()
                    val defaultUser = mapOf(
                        "id" to userId,
                        "name" to (currentUser.displayName ?: ""),
                        "username" to username,
                        "email" to (currentUser.email ?: ""),
                        "phoneNumber" to currentUser.phoneNumber,
                        "photoUrl" to currentUser.photoUrl?.toString(),
                        "bio" to bio,
                        "city" to city,
                        "joinDate" to currentTime,
                        "postCount" to 0,
                        "likeCount" to 0,
                        "commentCount" to 0,
                        "profileCompletionPercentage" to 20,
                        "createdAt" to currentTime,
                        "updatedAt" to currentTime
                    )
                    firestore.collection("users").document(userId).set(defaultUser).await()
                }
            }

            // Now apply the updates
            val updates = mapOf(
                "name" to name,
                "username" to username,
                "bio" to bio,
                "phoneNumber" to phoneNumber,
                "city" to city,
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "profileCompletionPercentage" to calculateProfileCompletion(
                    name = name,
                    username = username,
                    bio = bio,
                    phoneNumber = phoneNumber,
                    city = city,
                    photoUrl = userDoc.getString("photoUrl")
                )
            )

            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            // Return the updated profile
            getUserById(userId).collect { profileUser ->
                emit(profileUser)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getAchievements(): Flow<List<Achievement>> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val achievements = firestore.collection("users")
                .document(currentUser.uid)
                .collection("achievements")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Achievement(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        icon = doc.getString("icon") ?: "",
                        progress = doc.getLong("progress")?.toInt() ?: 0,
                        maxProgress = doc.getLong("maxProgress")?.toInt() ?: 100,
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        earnedDate = doc.getTimestamp("earnedDate")?.toDate()?.time
                    )
                }
            emit(achievements)
        } else {
            emit(emptyList())
        }
    }

    override fun getUserAchievements(userId: String): Flow<List<Achievement>> = flow {
        val achievements = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                Achievement(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    icon = doc.getString("icon") ?: "",
                    progress = doc.getLong("progress")?.toInt() ?: 0,
                    maxProgress = doc.getLong("maxProgress")?.toInt() ?: 100,
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    earnedDate = doc.getTimestamp("earnedDate")?.toDate()?.time
                )
            }
        emit(achievements)
    }

    override fun getRecentActivity(): Flow<List<Activity>> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val activities = firestore.collection("users")
                .document(currentUser.uid)
                .collection("activities")
                .orderBy("timestamp")
                .limit(10)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Activity(
                        id = doc.id,
                        type = ActivityType.valueOf(
                            doc.getString("type") ?: ActivityType.POST_CREATED.name
                        ),
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                            ?: System.currentTimeMillis(),
                        postId = doc.getString("postId")
                    )
                }
            emit(activities)
        } else {
            emit(emptyList())
        }
    }

    override fun getUserActivity(userId: String): Flow<List<Activity>> = flow {
        val activities = firestore.collection("users")
            .document(userId)
            .collection("activities")
            .orderBy("timestamp")
            .limit(10)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                Activity(
                    id = doc.id,
                    type = ActivityType.valueOf(
                        doc.getString("type") ?: ActivityType.POST_CREATED.name
                    ),
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                        ?: System.currentTimeMillis(),
                    postId = doc.getString("postId")
                )
            }
        emit(activities)
    }

    override fun getPosts(): Flow<List<Post>> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val posts = firestore.collection("posts")
                .whereEqualTo("authorId", currentUser.uid)
                .orderBy("timestamp")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        authorId = doc.getString("authorId") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                            ?: System.currentTimeMillis(),
                        likes = (doc.get("likes") as? List<String>) ?: emptyList(),
                        likeCount = doc.getLong("likeCount")?.toInt() ?: 0,
                        commentCount = doc.getLong("commentCount")?.toInt() ?: 0,
                        mediaUrls = doc.get("mediaUrls") as? List<String> ?: emptyList(),
                        category = doc.getString("category") ?: "",
                        city = doc.getString("city") ?: ""
                    )
                }
            emit(posts)
        } else {
            emit(emptyList())
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> = flow {
        val posts = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                Post(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    authorId = doc.getString("authorId") ?: "",
                    authorName = doc.getString("authorName") ?: "",
                    timestamp = doc.getTimestamp("timestamp")?.toDate()?.time
                        ?: System.currentTimeMillis(),
                    likes = (doc.get("likes") as? List<String>) ?: emptyList(),
                    likeCount = doc.getLong("likeCount")?.toInt() ?: 0,
                    commentCount = doc.getLong("commentCount")?.toInt() ?: 0,
                    mediaUrls = doc.get("mediaUrls") as? List<String> ?: emptyList(),
                    category = doc.getString("category") ?: "",
                    city = doc.getString("city") ?: ""
                )
            }
        emit(posts)
    }

    override suspend fun uploadProfilePhoto(photoUri: String): Flow<String> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val storageRef = storage.reference
                .child("profile_photos")
                .child(currentUser.uid)
                .child("profile.jpg")

            val uploadTask = storageRef.putFile(Uri.parse(photoUri)).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            firestore.collection("users")
                .document(currentUser.uid)
                .update("photoUrl", downloadUrl)
                .await()

            emit(downloadUrl)
        } else {
            throw Exception("User not authenticated")
        }
    }

    override suspend fun updateUserProfileImage(userId: String, imageUri: Uri): Flow<String> =
        flow {
            val storageRef = storage.reference
                .child("profile_photos")
                .child(userId)
                .child("profile.jpg")

            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            firestore.collection("users")
                .document(userId)
                .update("photoUrl", downloadUrl)
                .await()

            emit(downloadUrl)
        }

    override suspend fun deleteAccount(): Flow<Boolean> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Delete user data from Firestore
            firestore.collection("users")
                .document(currentUser.uid)
                .delete()
                .await()

            // Delete user's posts
            val posts = firestore.collection("posts")
                .whereEqualTo("authorId", currentUser.uid)
                .get()
                .await()

            posts.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Delete user's profile photo
            storage.reference
                .child("profile_photos")
                .child(currentUser.uid)
                .delete()
                .await()

            // Delete Firebase Auth account
            currentUser.delete().await()

            emit(true)
        } else {
            throw Exception("User not authenticated")
        }
    }

    override suspend fun deleteUserAccount(userId: String): Flow<Boolean> = flow {
        // Delete user data from Firestore
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()

        // Delete user's posts
        val posts = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .get()
            .await()

        posts.documents.forEach { doc ->
            doc.reference.delete().await()
        }

        // Delete user's profile photo
        storage.reference
            .child("profile_photos")
            .child(userId)
            .delete()
            .await()

        emit(true)
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
} 