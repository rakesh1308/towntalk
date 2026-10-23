package com.locup.mvp.domain.repository

import com.locup.mvp.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

/** Persistence port for area subscriptions ("tell me about posts near here"). */
interface SubscriptionRepository {

    fun observeSubscriptions(): Flow<List<Subscription>>

    suspend fun isSubscribed(clusterId: String): Boolean

    /** Adds the subscription if absent, removes it if present. */
    suspend fun toggle(clusterId: String, label: String)
}