package com.martovetskiy.ktorsample.domain.repository

import com.martovetskiy.ktorsample.domain.model.RefreshToken

interface TokenRepository {
    suspend fun save(token: RefreshToken): RefreshToken
    suspend fun findByToken(token: String): RefreshToken?
    suspend fun revokeById(id: Long)
    suspend fun revokeAllForUser(userId: Long)
}
