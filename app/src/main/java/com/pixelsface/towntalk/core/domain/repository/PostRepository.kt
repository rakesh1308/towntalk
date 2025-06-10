package com.pixelsface.towntalk.core.domain.repository

import com.pixelsface.towntalk.core.domain.model.Comment
import com.pixelsface.towntalk.core.domain.model.Post

interface PostRepository {
    suspend fun getPosts(city: String): Result<List<Post>>
    suspend fun getPost(id: String): Result<Post>
    suspend fun createPost(post: Post): Result<String>
    suspend fun likePost(id: String): Result<Unit>
    suspend fun unlikePost(id: String): Result<Unit>
    suspend fun addComment(postId: String, comment: Comment): Result<Unit>
    suspend fun deletePost(id: String): Result<Unit>
} 