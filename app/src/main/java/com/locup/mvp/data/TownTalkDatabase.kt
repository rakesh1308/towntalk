package com.locup.mvp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.locup.mvp.data.dao.PostDao
import com.locup.mvp.data.dao.SubscriptionDao
import com.locup.mvp.data.entity.PostEntity
import com.locup.mvp.data.entity.SubscriptionEntity

@Database(
    entities = [PostEntity::class, SubscriptionEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TownTalkDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        private const val DB_NAME = "towntalk.db"

        @Volatile
        private var instance: TownTalkDatabase? = null

        /**
         * Adds the `classifiedBy` column that records which inference engine
         * labelled each post. Defaults to the fallback so existing rows stay
         * valid and are honestly attributed.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE posts ADD COLUMN classifiedBy TEXT NOT NULL DEFAULT 'KeywordFallback'",
                )
            }
        }

        fun get(context: Context): TownTalkDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TownTalkDatabase::class.java,
                    DB_NAME,
                )
                    // A destructive fallback keeps the demo usable on a
                    // device where a future migration is missing, at the cost
                    // of demo rows — acceptable for seed data, never for prod.
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}