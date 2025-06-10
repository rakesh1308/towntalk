package com.pixelsface.towntalk.features.auth.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val bio: String? = null,
    val city: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
) : Parcelable 