package com.locup.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A 1km cluster the user has subscribed to. The MVP keeps just the
 * cluster id (e.g. "c_1297_3953") and a human-readable label captured
 * at the moment of subscription.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val clusterId: String,
    val label: String,
    val subscribedAt: Long
)
