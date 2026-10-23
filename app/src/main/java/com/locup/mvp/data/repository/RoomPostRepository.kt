package com.locup.mvp.data.repository

import com.locup.mvp.data.dao.PostDao
import com.locup.mvp.data.entity.PostEntity
import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.repository.LandmarkResolver
import com.locup.mvp.domain.repository.PostRepository
import com.locup.mvp.domain.repository.TextClassifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Room-backed [PostRepository].
 *
 * Classification runs on [classificationDispatcher] rather than the caller's
 * context because it loads a model and executes native inference; doing that
 * on the main thread risks an ANR on first use.
 */
class RoomPostRepository(
    private val postDao: PostDao,
    private val classifier: TextClassifier,
    private val landmarks: LandmarkResolver,
    private val classificationDispatcher: CoroutineDispatcher,
) : PostRepository {

    override fun observePosts(): Flow<List<Post>> =
        postDao.observeAll()
            .map { rows -> rows.map(PostEntity::toDomain) }
            .flowOn(classificationDispatcher)

    override suspend fun publish(text: String, location: GeoPoint, author: String) {
        val result = withContext(classificationDispatcher) { classifier.classify(text) }

        val post = Post(
            id = "post_${UUID.randomUUID()}",
            author = author,
            text = text.trim(),
            category = result.topCategory,
            modelConfidence = result.confidence,
            location = location,
            clusterId = location.clusterId,
            areaLabel = landmarks.labelFor(location.latitude, location.longitude),
            createdAt = System.currentTimeMillis(),
            upCount = 0,
            downCount = 0,
            // Persist which engine actually ran, so the row stays honest even
            // if the model is later removed from the APK.
            classifiedBy = result.engine,
        )
        postDao.upsert(PostEntity.fromDomain(post))
    }

    override suspend fun upvote(postId: String) = postDao.upvote(postId)

    override suspend fun downvote(postId: String) = postDao.downvote(postId)

    override suspend fun seedDemoDataIfEmpty() {
        if (postDao.count() > 0) return
        // Offsets are negative (minutes ago); stamp them against wall clock so
        // the seeded rows sort correctly relative to anything posted later.
        val now = System.currentTimeMillis()
        DEMO_POSTS.forEach { seed ->
            postDao.upsert(PostEntity.fromDomain(seed.copy(createdAt = now + seed.createdAt)))
        }
    }

    private companion object {
        /**
         * Five sample posts spanning every category, clustered around central
         * Bengaluru so the "Here" filter has content without a GPS fix.
         *
         * Ids carry the `demo_` prefix and authors say "(sample)" so the UI
         * can badge them and a reviewer is never misled into thinking these
         * are real reports.
         */
        fun demo(
            index: Int,
            author: String,
            text: String,
            category: PostCategory,
            confidence: Float,
            point: GeoPoint,
            area: String,
            minutesAgo: Long,
            up: Int,
            down: Int,
        ) = Post(
            id = "${Post.DEMO_ID_PREFIX}$index",
            author = "$author (sample)",
            text = text,
            category = category,
            modelConfidence = confidence,
            location = point,
            clusterId = point.clusterId,
            areaLabel = area,
            createdAt = -minutesAgo * 60_000L,
            upCount = up,
            downCount = down,
            classifiedBy = InferenceEngine.TfliteEmbeddingClassifier,
        )

        val DEMO_POSTS = listOf(
            demo(
                1, "Asha", "Waterlogging near the underpass, drive around.",
                PostCategory.Emergency, 0.91f, GeoPoint(12.97, 77.59), "MG Road",
                4, 6, 1,
            ),
            demo(
                2, "Ravi", "Heavy traffic on MG Road, avoid until 7pm.",
                PostCategory.Traffic, 0.83f, GeoPoint(12.97, 77.60), "MG Road",
                11, 3, 0,
            ),
            demo(
                3, "Meera", "Street food festival at Cubbon Park this weekend!",
                PostCategory.Event, 0.76f, GeoPoint(12.97, 77.595), "Cubbon Park",
                28, 9, 2,
            ),
            demo(
                4, "Kunal", "Garbage pile on 5th main, smells awful.",
                PostCategory.Civic, 0.69f, GeoPoint(12.97, 77.60), "MG Road",
                42, 4, 3,
            ),
            demo(
                5, "Priya", "Free yoga class at the community hall tomorrow.",
                PostCategory.General, 0.62f, GeoPoint(12.97, 77.59), "MG Road",
                90, 1, 0,
            ),
        )
    }
}