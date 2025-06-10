package com.pixelsface.towntalk.features.feed.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.pixelsface.towntalk.core.common.error.AuthorizationException
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.feed.data.model.CommentDto
import com.pixelsface.towntalk.features.feed.data.model.PostDto
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Firebase implementation of the PostRepository interface.
 */
@Singleton
class FirebasePostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : PostRepository {

    override suspend fun getPosts(city: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("city", city)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.copy(id = doc.id)?.toDomain()
            }
            
            Result.success(posts)
        } catch (e: Exception) {
            Result.error<List<Post>>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun getPost(id: String): Result<Post> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("posts").document(id).get().await()
            val postDto = doc.toObject(PostDto::class.java)?.copy(id = doc.id)
            
            if (postDto != null) {
                Result.success(postDto.toDomain())
            } else {
                Result.error<Post>(UnknownException("Post not found"))
            }
        } catch (e: Exception) {
            Result.error<Post>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun createPost(post: Post): Result<String> = withContext(Dispatchers.IO) {
        try {
            val postDto = PostDto.fromDomain(post)
            val postId = UUID.randomUUID().toString()
            
            firestore.collection("posts")
                .document(postId)
                .set(postDto.copy(id = postId))
                .await()
            
            Result.success(postId)
        } catch (e: Exception) {
            Result.error<String>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun likePost(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.error<Unit>(AuthorizationException("User not authenticated"))
            val postRef = firestore.collection("posts").document(id)

            firestore.runTransaction {
                transaction ->
                transaction.update(postRef, "likes", FieldValue.arrayUnion(userId))
                transaction.update(postRef, "likeCount", FieldValue.increment(1))
                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException("Failed to like post: ${e.message}", e))
        }
    }
    
    override suspend fun unlikePost(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.error<Unit>(AuthorizationException("User not authenticated"))
            val postRef = firestore.collection("posts").document(id)

            firestore.runTransaction {
                transaction ->
                transaction.update(postRef, "likes", FieldValue.arrayRemove(userId))
                transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException("Failed to unlike post: ${e.message}", e))
        }
    }
    
    override suspend fun addComment(postId: String, comment: Comment): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val commentDto = CommentDto.fromDomain(comment)
            val commentId = UUID.randomUUID().toString()
            
            // Add comment
            firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
                .set(commentDto.copy(id = commentId))
                .await()
            
            // Increment comments count
            firestore.collection("posts")
                .document(postId)
                .update("comments", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun deletePost(postId: String/*, userId: String*/): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val postDocRef = firestore.collection("posts").document(postId)

            val postSnapshot = postDocRef.get().await()
            val post = postSnapshot.toObject(PostDto::class.java)?.toDomain()
                ?: return@withContext Result.Error(UnknownException("Post not found (ID: $postId) or an error occurred retrieving it."))

            // Authorization check is now primarily in the UseCase.

            // 1. Delete associated comments from the subcollection posts/{postId}/comments
            Log.d("DeletePost", "Attempting to delete comments from subcollection for postId: $postId")
            val commentsCollectionRef = postDocRef.collection("comments")
            val commentsSnapshot = commentsCollectionRef.get().await()
            if (commentsSnapshot.isEmpty) {
                Log.d("DeletePost", "No comments found in subcollection for postId: $postId")
            } else {
                Log.d("DeletePost", "Found ${commentsSnapshot.size()} comments in subcollection to delete for postId: $postId")
                val commentDeletions = commentsSnapshot.documents.map {
                    Log.d("DeletePost", "Deleting commentId: ${it.id} from subcollection of postId: $postId")
                    async { it.reference.delete().await() }
                }
                commentDeletions.awaitAll()
                Log.d("DeletePost", "Finished deleting comments from subcollection for postId: $postId")
            }

            // 2. Delete associated media from Firebase Storage
            if (post.mediaUrls.isNotEmpty()) {
                Log.d("DeletePost", "Attempting to delete ${post.mediaUrls.size} media files for postId: $postId")
                val storage = FirebaseStorage.getInstance()
                val mediaDeletions = post.mediaUrls.mapNotNull { mediaUrl ->
                    if (mediaUrl.isNotBlank()) {
                        async {
                            try {
                                Log.d("DeletePost", "Attempting to delete media URL: $mediaUrl")
                                val storageRef = storage.getReferenceFromUrl(mediaUrl)
                                storageRef.delete().await()
                                Log.i("DeletePost", "Successfully deleted media: $mediaUrl")
                            } catch (e: Exception) {
                                Log.e("DeletePost", "Failed to delete media $mediaUrl for postId $postId: ${e.message}", e)
                            }
                        }
                    } else {
                        Log.w("DeletePost", "Encountered blank media URL for postId: $postId")
                        null
                    }
                }
                if (mediaDeletions.isNotEmpty()) {
                   mediaDeletions.awaitAll()
                }
                Log.d("DeletePost", "Finished attempting to delete media for postId: $postId")
            } else {
                Log.d("DeletePost", "No media URLs found for postId: $postId")
            }

            // 3. Delete the post document itself
            Log.d("DeletePost", "Attempting to delete post document for postId: $postId")
            postDocRef.delete().await()
            Log.i("DeletePost", "Successfully deleted post document for postId: $postId")

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("DeletePost", "Generic error during deletePost for postId $postId: ${e.message}", e)
            Result.Error(UnknownException("Failed to delete post (ID: $postId): ${e.message}", e))
        }
    }
    
    override suspend fun deleteComment(postId: String, commentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = firestore.batch()
            
            // Delete comment
            val commentRef = firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)
            batch.delete(commentRef)
            
            // Decrement post comment count
            val postRef = firestore.collection("posts").document(postId)
            batch.update(postRef, "comments", com.google.firebase.firestore.FieldValue.increment(-1))
            
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun getPostComments(postId: String): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val comments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentDto::class.java)?.copy(id = doc.id)?.toDomain()
            }
            
            Result.success(comments)
        } catch (e: Exception) {
            Result.error<List<Comment>>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun getPostLikes(postId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("posts").document(postId).get().await()
            val postDto = doc.toObject(PostDto::class.java)
            
            if (postDto != null) {
                Result.success(postDto.likes)
            } else {
                Result.error<List<String>>(UnknownException("Post not found"))
            }
        } catch (e: Exception) {
            Result.error<List<String>>(UnknownException(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun reportPost(postId: String, reporterUserId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val reportId = UUID.randomUUID().toString()
            val reportData = hashMapOf(
                "postId" to postId,
                "reporterUserId" to reporterUserId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("post_reports")
                .document(reportId)
                .set(reportData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error<Unit>(UnknownException(e.message ?: "Failed to report post", e))
        }
    }

    override suspend fun searchPosts(query: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.Success(emptyList<Post>())
            }

            val queryKeywords = query.lowercase().split(" ").filter { it.length > 1 }.distinct().take(10)
            if (queryKeywords.isEmpty()) {
                return@withContext Result.Success(emptyList<Post>())
            }

            // Firestore `array-contains-any` can check for up to 10 values.
            // For a more robust solution with more keywords or OR conditions across multiple `array-contains`,
            // you might need multiple queries and client-side merging, or a backend solution.
            val snapshot = firestore.collection("posts")
                .whereArrayContainsAny("searchKeywords", queryKeywords)
                // Optionally, order by another field like timestamp or likeCount if desired for search results
                 .orderBy("likeCount", Query.Direction.DESCENDING) // Example: order by popularity
                .limit(20) // Limit results to avoid fetching too much data
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.copy(id = doc.id)?.toDomain()
            }

            Result.Success(posts)
        } catch (e: Exception) {
            Log.e("FirebasePostRepo", "Failed to search posts: ${e.message}", e)
            if (e.message?.contains("index") == true && e.message?.contains("searchKeywords") == true) {
                Result.Error(UnknownException("Failed to search posts: Firestore index missing for 'searchKeywords'. Please create it in the Firebase console.", e))
            } else {
                Result.Error(UnknownException("Failed to search posts: ${e.message}", e))
            }
        }
    }

    // TODO: Optimize this trending posts logic. Fetching all posts and sorting client-side by likes is not scalable.
    // Consider maintaining a separate 'trending_posts' collection updated by Cloud Functions,
    // or using a more sophisticated scoring mechanism that can be queried more efficiently.
    // For now, we sort by the size of the likes list (number of likes).
    override suspend fun getTrendingPosts(limit: Int): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("posts")
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong()) // Firestore limit is long
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.copy(id = doc.id)?.toDomain()
            }
            // Client-side sorting is no longer needed
            Result.Success(posts)
        } catch (e: Exception) {
            Log.e("FirebasePostRepo", "Failed to get trending posts: ${e.message}", e)
            // Check if the exception is due to a missing index
            if (e.message?.contains("index") == true && e.message?.contains("likeCount") == true) {
                 Result.Error(UnknownException("Failed to get trending posts: Firestore index missing for 'likeCount'. Please create it in the Firebase console.", e))
            } else {
                 Result.Error(UnknownException("Failed to get trending posts: ${e.message}", e))
            }
        }
    }
} 