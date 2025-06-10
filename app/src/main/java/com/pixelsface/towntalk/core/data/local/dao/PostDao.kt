package com.pixelsface.towntalk.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelsface.towntalk.core.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE city = :city ORDER BY timestamp DESC")
    suspend fun getPostsByCity(city: String): List<PostEntity>
    
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?
    
    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY timestamp DESC")
    suspend fun getPostsByAuthor(authorId: String): List<PostEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)
    
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
    
    @Query("UPDATE posts SET likes = :likes WHERE id = :postId")
    suspend fun updateLikes(postId: String, likes: List<String>)
    
    @Query("UPDATE posts SET comments = comments + 1 WHERE id = :postId")
    suspend fun incrementComments(postId: String)
    
    @Query("UPDATE posts SET comments = comments - 1 WHERE id = :postId")
    suspend fun decrementComments(postId: String)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
} 