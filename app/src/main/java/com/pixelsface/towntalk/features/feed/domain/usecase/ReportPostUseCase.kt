package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Use case for reporting a post.
 */
class ReportPostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case to report a post.
     * @param postId The ID of the post to report.
     * @param reporterUserId The ID of the user submitting the report.
     * @param reason The reason for the report.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(postId: String, reporterUserId: String, reason: String): Result<Unit> {
        if (reason.isBlank()) {
            return Result.Error(ValidationException("Reason for reporting cannot be empty."))
        }
        // Add other validations for reason if needed (e.g., min/max length)

        if (reporterUserId.isBlank()){
            return Result.Error(ValidationException("User must be logged in to report a post."))
        }

        return postRepository.reportPost(postId, reporterUserId, reason)
    }
} 