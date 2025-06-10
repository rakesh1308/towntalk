package com.pixelsface.towntalk.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelsface.towntalk.core.data.local.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    fun getCommentsByPostId(postId: String): Flow<List<CommentEntity>>
    
    @Query("SELECT * FROM comments WHERE id = :commentId")
    fun getCommentById(commentId: String): Flow<CommentEntity?>
    
    @Query("SELECT * FROM comments WHERE authorId = :authorId ORDER BY timestamp DESC")
    fun getCommentsByAuthor(authorId: String): Flow<List<CommentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)
    
    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)
    
    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: String)
} 