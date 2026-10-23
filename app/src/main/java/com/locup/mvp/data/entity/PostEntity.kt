package com.locup.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.model.PostCategory

/**
 * Room row for a post.
 *
 * Deliberately separate from the domain [Post]: this class owns persistence
 * concerns (annotations, defaults, mapping) while [Post] stays a plain Kotlin
 * model. The mapping lives here so no layer has to know about both.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val text: String,
    /** Raw label string as emitted by the classifier. */
    val category: String,
    /** Model confidence in 0..1, fixed at publish time. */
    val modelConfidence: Float,
    val lat: Double,
    val lon: Double,
    val clusterId: String,
    val areaLabel: String,
    val createdAt: Long,
    val upCount: Int = 0,
    val downCount: Int = 0,
    /**
     * Which engine produced [category], so the UI can keep telling the truth
     * about a row classified years ago under different circumstances.
     */
    val classifiedBy: String = InferenceEngine.KeywordFallback.name,
) {
    fun toDomain(): Post = Post(
        id = id,
        author = author,
        text = text,
        category = PostCategory.fromAssetLabelOrGeneral(category),
        modelConfidence = modelConfidence,
        location = GeoPoint(lat, lon),
        clusterId = clusterId,
        areaLabel = areaLabel,
        createdAt = createdAt,
        upCount = upCount,
        downCount = downCount,
        classifiedBy = runCatching { InferenceEngine.valueOf(classifiedBy) }
            .getOrDefault(InferenceEngine.KeywordFallback),
    )

    companion object {
        fun fromDomain(post: Post): PostEntity = PostEntity(
            id = post.id,
            author = post.author,
            text = post.text,
            category = post.category.name,
            modelConfidence = post.modelConfidence,
            lat = post.location.latitude,
            lon = post.location.longitude,
            clusterId = post.clusterId,
            areaLabel = post.areaLabel,
            createdAt = post.createdAt,
            upCount = post.upCount,
            downCount = post.downCount,
            classifiedBy = post.classifiedBy.name,
        )
    }
}