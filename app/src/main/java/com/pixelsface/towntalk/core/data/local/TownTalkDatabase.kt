package com.pixelsface.towntalk.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pixelsface.towntalk.core.data.local.converter.Converters
import com.pixelsface.towntalk.core.data.local.dao.CommentDao
import com.pixelsface.towntalk.core.data.local.dao.PostDao
import com.pixelsface.towntalk.core.data.local.dao.UserDao
import com.pixelsface.towntalk.core.data.local.entity.CommentEntity
import com.pixelsface.towntalk.core.data.local.entity.PostEntity
import com.pixelsface.towntalk.core.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TownTalkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    
    companion object {
        const val DATABASE_NAME = "towntalk_db"
    }
} 