package com.locup.mvp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.locup.mvp.data.dao.PostDao
import com.locup.mvp.data.dao.SubscriptionDao
import com.locup.mvp.data.entity.PostEntity
import com.locup.mvp.data.entity.SubscriptionEntity

@Database(
    entities = [PostEntity::class, SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TownTalkDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile private var INSTANCE: TownTalkDatabase? = null

        fun get(context: Context): TownTalkDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TownTalkDatabase::class.java,
                    "locup.db"
                ).build().also { INSTANCE = it }
            }
    }
}
