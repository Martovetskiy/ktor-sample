package com.martovetskiy.ktorsample.domain.model

data class RefreshToken(
    val id: Long = 0,
    val userId: Long,
    val token: String,
    val expiresAt: Long
)
