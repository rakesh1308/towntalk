package com.pixelsface.towntalk.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixelsface.towntalk.core.common.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            photoUrl = photoUrl,
            city = city,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                photoUrl = user.photoUrl,
                city = user.city,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
} 