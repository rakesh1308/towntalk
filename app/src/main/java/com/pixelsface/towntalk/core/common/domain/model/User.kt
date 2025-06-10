package com.pixelsface.towntalk.core.common.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long
) 