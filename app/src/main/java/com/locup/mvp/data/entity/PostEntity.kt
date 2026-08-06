package com.locup.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A post published to a 1km geo-cluster. The local on-device classifier
 * sets [category] + [modelConfidence] at create time. Crowd-sourced
 * [upCount] / [downCount] drive the displayed Post Confidence
 * (= upCount - downCount).
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val text: String,
    val category: String,
    val modelConfidence: Float,
    val lat: Double,
    val lon: Double,
    val clusterId: String,
    val areaLabel: String,
    val createdAt: Long,
    val upCount: Int = 0,
    val downCount: Int = 0
) {
    val confidence: Int get() = upCount - downCount
}
