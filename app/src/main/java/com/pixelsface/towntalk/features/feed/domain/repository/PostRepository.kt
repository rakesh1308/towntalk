package com.pixelsface.towntalk.features.feed.domain.repository

import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.core.common.error.Result

/**
 * Repository interface for post-related operations.
 */
interface PostRepository {
    /**
     * Get posts for a specific city.
     * @param city The city to get posts for.
     * @return A Result containing a list of posts or an error.
     */
    suspend fun getPosts(city: String): Result<List<Post>>
    
    /**
     * Get a specific post by ID.
     * @param id The ID of the post to get.
     * @return A Result containing the post or an error.
     */
    suspend fun getPost(id: String): Result<Post>
    
    /**
     * Create a new post.
     * @param post The post to create.
     * @return A Result containing the ID of the created post or an error.
     */
    suspend fun createPost(post: Post): Result<String>
    
    /**
     * Like a post.
     * @param id The ID of the post to like.
     * @return A Result indicating success or failure.
     */
    suspend fun likePost(id: String): Result<Unit>
    
    /**
     * Unlike a post.
     * @param id The ID of the post to unlike.
     * @return A Result indicating success or failure.
     */
    suspend fun unlikePost(id: String): Result<Unit>
    
    /**
     * Add a comment to a post.
     * @param postId The ID of the post to comment on.
     * @param comment The comment to add.
     * @return A Result indicating success or failure.
     */
    suspend fun addComment(postId: String, comment: Comment): Result<Unit>
    
    /**
     * Delete a comment from a post.
     * @param postId The ID of the post containing the comment.
     * @param commentId The ID of the comment to delete.
     * @return A Result indicating success or failure.
     */
    suspend fun deleteComment(postId: String, commentId: String): Result<Unit>
    
    /**
     * Delete a post and all its associated data (comments, media).
     * The implementation should ensure that only the author of the post can delete it.
     * @param postId The ID of the post to delete.
     * @param userId The ID of the user attempting to delete the post (for authorization).
     * @return A Result indicating success or failure.
     */
    suspend fun deletePost(postId: String, /*userId: String*/): Result<Unit>
    
    /**
     * Get comments for a post.
     * @param postId The ID of the post to get comments for.
     * @return A Result containing a list of comments or an error.
     */
    suspend fun getPostComments(postId: String): Result<List<Comment>>
    
    /**
     * Get likes for a post.
     * @param postId The ID of the post to get likes for.
     * @return A Result containing a list of user IDs who liked the post or an error.
     */
    suspend fun getPostLikes(postId: String): Result<List<String>>

    /**
     * Report a post.
     * @param postId The ID of the post to report.
     * @param reporterUserId The ID of the user reporting the post.
     * @param reason The reason for reporting the post.
     * @return A Result indicating success or failure.
     */
    suspend fun reportPost(postId: String, reporterUserId: String, reason: String): Result<Unit>

    /**
     * Search for posts based on a query string.
     * The query will be matched against post titles and content.
     * @param query The search query.
     * @return A Result containing a list of matching posts or an error.
     */
    suspend fun searchPosts(query: String): Result<List<Post>>

    /**
     * Get trending posts.
     * @param limit The maximum number of trending posts to return.
     * @return A Result containing a list of trending posts or an error.
     */
    suspend fun getTrendingPosts(limit: Int): Result<List<Post>>
} 