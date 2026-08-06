package com.locup.mvp.repo

import android.content.Context
import com.locup.mvp.classifier.PostClassifierRepository
import com.locup.mvp.data.TownTalkDatabase
import com.locup.mvp.data.entity.PostEntity
import com.locup.mvp.data.entity.SubscriptionEntity
import com.locup.mvp.model.Cluster
import kotlinx.coroutines.flow.Flow

/**
 * Thin facade over the DAOs + on-device classifier. Kept dependency-free
 * (no Hilt) so the MVP stays easy to read end-to-end.
 */
class LocUpRepository(
    private val db: TownTalkDatabase,
    private val classifier: PostClassifierRepository
) {

    val isRealModelActive: Boolean get() = classifier.isRealModelActive

    fun observePosts(): Flow<List<PostEntity>> = db.postDao().observeAll()
    fun observeSubscriptions(): Flow<List<SubscriptionEntity>> = db.subscriptionDao().observeAll()

    suspend fun publish(text: String, lat: Double, lon: Double, author: String = "You") {
        val clusterId = Cluster.idFor(lat, lon)
        val r = classifier.classify(text)
        val post = PostEntity(
            id = "local_${System.currentTimeMillis()}",
            author = author,
            text = text,
            category = r.label,
            modelConfidence = r.confidence,
            lat = lat,
            lon = lon,
            clusterId = clusterId,
            areaLabel = LandmarkRepository.labelFor(lat, lon),
            createdAt = System.currentTimeMillis(),
            upCount = 0,
            downCount = 0
        )
        db.postDao().upsert(post)
    }

    fun liveClassify(text: String) = classifier.classify(text)

    suspend fun upvote(postId: String) = db.postDao().upvote(postId)
    suspend fun downvote(postId: String) = db.postDao().downvote(postId)

    suspend fun toggleSubscription(clusterId: String, label: String) {
        if (db.subscriptionDao().isSubscribed(clusterId)) {
            db.subscriptionDao().unsubscribe(clusterId)
        } else {
            db.subscriptionDao().subscribe(
                SubscriptionEntity(clusterId, label, System.currentTimeMillis())
            )
        }
    }

    suspend fun seedSampleDataIfEmpty() {
        if (db.postDao().count() > 0) return
        val now = System.currentTimeMillis()
        listOf(
            PostEntity(
                id = "p1",
                author = "Asha",
                text = "Waterlogging near the underpass, drive around.",
                category = "Emergency",
                modelConfidence = 0.91f,
                lat = 12.97, lon = 77.59,
                clusterId = Cluster.idFor(12.97, 77.59),
                areaLabel = LandmarkRepository.labelFor(12.97, 77.59),
                createdAt = now - 4 * 60_000,
                upCount = 6, downCount = 1
            ),
            PostEntity(
                id = "p2",
                author = "Ravi",
                text = "Heavy traffic on MG Road, avoid until 7pm.",
                category = "Traffic",
                modelConfidence = 0.83f,
                lat = 12.97, lon = 77.60,
                clusterId = Cluster.idFor(12.97, 77.60),
                areaLabel = LandmarkRepository.labelFor(12.97, 77.60),
                createdAt = now - 11 * 60_000,
                upCount = 3, downCount = 0
            ),
            PostEntity(
                id = "p3",
                author = "Meera",
                text = "Street food festival at Cubbon Park this weekend!",
                category = "Event",
                modelConfidence = 0.76f,
                lat = 12.97, lon = 77.595,
                clusterId = Cluster.idFor(12.97, 77.595),
                areaLabel = LandmarkRepository.labelFor(12.97, 77.595),
                createdAt = now - 28 * 60_000,
                upCount = 9, downCount = 2
            ),
            PostEntity(
                id = "p4",
                author = "Kunal",
                text = "Garbage pile on 5th main, smells awful.",
                category = "Civic",
                modelConfidence = 0.69f,
                lat = 12.97, lon = 77.60,
                clusterId = Cluster.idFor(12.97, 77.60),
                areaLabel = LandmarkRepository.labelFor(12.97, 77.60),
                createdAt = now - 42 * 60_000,
                upCount = 4, downCount = 3
            ),
            PostEntity(
                id = "p5",
                author = "Priya",
                text = "Free yoga class at the community hall tomorrow.",
                category = "General",
                modelConfidence = 0.62f,
                lat = 12.97, lon = 77.59,
                clusterId = Cluster.idFor(12.97, 77.59),
                areaLabel = LandmarkRepository.labelFor(12.97, 77.59),
                createdAt = now - 90 * 60_000,
                upCount = 1, downCount = 0
            )
        ).forEach { db.postDao().upsert(it) }
    }

    fun close() {
        classifier.close()
    }

    companion object {
        fun create(context: Context, classifier: PostClassifierRepository): LocUpRepository =
            LocUpRepository(TownTalkDatabase.get(context), classifier)
    }
}
