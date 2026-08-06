package com.locup.mvp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.locup.mvp.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PostEntity)

    @Query("UPDATE posts SET upCount = upCount + 1 WHERE id = :id")
    suspend fun upvote(id: String)

    @Query("UPDATE posts SET downCount = downCount + 1 WHERE id = :id")
    suspend fun downvote(id: String)
}
