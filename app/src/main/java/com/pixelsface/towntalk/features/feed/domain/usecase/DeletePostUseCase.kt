package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.AuthorizationException
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Use case for deleting a post.
 * Ensures that only the author of the post can delete it.
 */
class DeletePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Result<Unit> {
        // First, get the post to verify the author
        val postResult = postRepository.getPost(postId)

        return when (postResult) {
            is Result.Success -> {
                val post = postResult.data
                if (post.authorId == userId) {
                    // User is authorized, proceed with deletion
                    postRepository.deletePost(postId)
                } else {
                    Result.Error(AuthorizationException("User not authorized to delete this post."))
                }
            }
            is Result.Error -> {
                // Propagate the error from getPost (e.g., post not found)

                Result.Error(UnknownException("User not authorized to delete this post.",postResult.exception))
            }
        }
    }
} 