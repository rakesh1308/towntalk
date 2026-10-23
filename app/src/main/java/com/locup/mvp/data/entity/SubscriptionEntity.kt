package com.locup.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.locup.mvp.domain.model.Subscription

/**
 * A ~1 km cluster the user has subscribed to.
 *
 * Stores only the cluster id plus a label captured at subscribe time: the
 * label is a convenience for display, and re-deriving it later would make a
 * subscription silently change meaning when the landmark table changes.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val clusterId: String,
    val label: String,
    val subscribedAt: Long,
) {
    fun toDomain(): Subscription = Subscription(clusterId, label, subscribedAt)

    companion object {
        fun fromDomain(subscription: Subscription): SubscriptionEntity = SubscriptionEntity(
            clusterId = subscription.clusterId,
            label = subscription.label,
            subscribedAt = subscription.subscribedAt,
        )
    }
}