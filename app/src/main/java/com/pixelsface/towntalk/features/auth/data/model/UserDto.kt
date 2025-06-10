package com.pixelsface.towntalk.features.auth.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.pixelsface.towntalk.features.auth.domain.model.User
import java.util.Date

@IgnoreExtraProperties
data class UserDto(
    @DocumentId
    val docId: String = "",
    @PropertyName("name")
    val name: String = "",
    @PropertyName("email")
    val email: String = "",
    @PropertyName("photo_url")
    val photoUrl: String? = null,
    @PropertyName("bio")
    val bio: String? = null,
    @PropertyName("city")
    val city: String? = null,
    @ServerTimestamp
    @PropertyName("created_at")
    val createdAt: Date? = null,
    @ServerTimestamp
    @PropertyName("updated_at")
    val updatedAt: Date? = null,
    @PropertyName("is_online")
    val isOnline: Boolean? = null,
    @ServerTimestamp
    @PropertyName("last_seen")
    val lastSeen: Date? = null
)

fun UserDto.toDomain(): User {
    return User(
        id = this.docId,
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
        bio = this.bio,
        city = this.city,
        createdAt = this.createdAt?.time ?: 0L,
        updatedAt = this.updatedAt?.time ?: 0L,
        isOnline = this.isOnline ?: false,
        lastSeen = this.lastSeen?.time
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        docId = this.id,
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
        bio = this.bio,
        city = this.city,
        createdAt = if (this.createdAt == 0L) null else Date(this.createdAt),
        updatedAt = if (this.updatedAt == 0L) null else Date(this.updatedAt),
        isOnline = this.isOnline,
        lastSeen = this.lastSeen?.let { Date(it) }
    )
} 