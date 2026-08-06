package com.locup.mvp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.locup.mvp.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun subscribe(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE clusterId = :clusterId")
    suspend fun unsubscribe(clusterId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE clusterId = :clusterId)")
    suspend fun isSubscribed(clusterId: String): Boolean
}
