package com.pixelsface.towntalk.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelsface.towntalk.core.domain.model.Comment
import com.pixelsface.towntalk.core.domain.model.Post
import com.pixelsface.towntalk.core.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebasePostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
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
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }
            
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(id: String): Result<Post> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("posts")
                .document(id)
                .get()
                .await()
            
            val post = doc.toObject(Post::class.java)
                ?: throw Exception("Post not found")
            
            Result.success(post.copy(id = doc.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPost(post: Post): Result<String> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("posts")
                .add(post)
                .await()
            
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likePost(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            
            firestore.collection("posts")
                .document(id)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikePost(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            
            firestore.collection("posts")
                .document(id)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addComment(postId: String, comment: Comment): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = firestore.batch()
            
            // Add comment
            val commentRef = firestore.collection("comments").document()
            batch.set(commentRef, comment.copy(id = commentRef.id))
            
            // Update post comment count
            val postRef = firestore.collection("posts").document(postId)
            batch.update(postRef, "comments", com.google.firebase.firestore.FieldValue.increment(1))
            
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            
            val post = getPost(id).getOrNull()
                ?: throw Exception("Post not found")
            
            if (post.authorId != user.uid) {
                throw Exception("Not authorized to delete this post")
            }
            
            firestore.collection("posts")
                .document(id)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 