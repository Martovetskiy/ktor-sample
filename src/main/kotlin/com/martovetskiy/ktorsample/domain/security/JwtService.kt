package com.martovetskiy.ktorsample.domain.security

import com.auth0.jwt.interfaces.JWTVerifier
import com.martovetskiy.ktorsample.domain.model.User

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

interface JwtService {
    val verifier: JWTVerifier
    suspend fun issueTokensFor(user: User): TokenPair
    suspend fun refreshAccessToken(refreshToken: String): TokenPair
}
