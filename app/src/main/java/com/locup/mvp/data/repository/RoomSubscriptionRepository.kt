package com.locup.mvp.data.repository

import com.locup.mvp.data.dao.SubscriptionDao
import com.locup.mvp.data.entity.SubscriptionEntity
import com.locup.mvp.domain.model.Subscription
import com.locup.mvp.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [SubscriptionRepository]. */
class RoomSubscriptionRepository(
    private val dao: SubscriptionDao,
) : SubscriptionRepository {

    override fun observeSubscriptions(): Flow<List<Subscription>> =
        dao.observeAll().map { rows -> rows.map(SubscriptionEntity::toDomain) }

    override suspend fun isSubscribed(clusterId: String): Boolean =
        dao.isSubscribed(clusterId)

    override suspend fun toggle(clusterId: String, label: String) {
        if (dao.isSubscribed(clusterId)) {
            dao.unsubscribe(clusterId)
        } else {
            dao.subscribe(SubscriptionEntity(clusterId, label, System.currentTimeMillis()))
        }
    }
}