package com.martovetskiy.ktorsample.data.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.martovetskiy.ktorsample.domain.model.RefreshToken
import com.martovetskiy.ktorsample.domain.model.User
import com.martovetskiy.ktorsample.domain.repository.TokenRepository
import com.martovetskiy.ktorsample.domain.security.JwtService
import com.martovetskiy.ktorsample.domain.security.TokenPair
import java.util.*

class JwtServiceImpl(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTokenTTL: Long,
    private val refreshTokenTTL: Long,
    private val tokenRepository: TokenRepository
) : JwtService {
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    override val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    
    override suspend fun issueTokensFor(user: User): TokenPair {
        // Revoke all existing refresh tokens for this user
        tokenRepository.revokeAllForUser(user.id)
        
        val now = System.currentTimeMillis()
        
        // Create access token
        val accessToken = JWT.create()
            .withSubject(user.id.toString())
            .withClaim("username", user.username)
            .withIssuer(issuer)
            .withAudience(audience)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + accessTokenTTL * 1000))
            .sign(algorithm)
        
        // Create refresh token (also include username)
        val refreshTokenStr = JWT.create()
            .withSubject(user.id.toString())
            .withClaim("username", user.username)
            .withIssuer(issuer)
            .withAudience(audience)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + refreshTokenTTL * 1000))
            .sign(algorithm)
        
        // Store refresh token in database
        val refreshToken = RefreshToken(
            userId = user.id,
            token = refreshTokenStr,
            expiresAt = now + refreshTokenTTL * 1000
        )
        tokenRepository.save(refreshToken)
        
        return TokenPair(accessToken, refreshTokenStr)
    }
    
    override suspend fun refreshAccessToken(refreshToken: String): TokenPair {
        // Verify and decode refresh token
        val decoded = try {
            verifier.verify(refreshToken)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid refresh token")
        }
        
        val userId = decoded.subject.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid token subject")
        
        val username = decoded.getClaim("username").asString()
            ?: throw IllegalArgumentException("Invalid token: missing username")
        
        // Check if token exists in database
        val storedToken = tokenRepository.findByToken(refreshToken)
            ?: throw IllegalArgumentException("Refresh token not found or revoked")
        
        // Check if token is expired
        if (storedToken.expiresAt < System.currentTimeMillis()) {
            tokenRepository.revokeById(storedToken.id)
            throw IllegalArgumentException("Refresh token expired")
        }
        
        // Revoke old refresh token
        tokenRepository.revokeById(storedToken.id)
        
        val now = System.currentTimeMillis()
        
        // Create new access token
        val accessToken = JWT.create()
            .withSubject(userId.toString())
            .withClaim("username", username)
            .withIssuer(issuer)
            .withAudience(audience)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + accessTokenTTL * 1000))
            .sign(algorithm)
        
        // Create new refresh token
        val newRefreshTokenStr = JWT.create()
            .withSubject(userId.toString())
            .withClaim("username", username)
            .withIssuer(issuer)
            .withAudience(audience)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + refreshTokenTTL * 1000))
            .sign(algorithm)
        
        // Store new refresh token in database
        val newRefreshToken = RefreshToken(
            userId = userId,
            token = newRefreshTokenStr,
            expiresAt = now + refreshTokenTTL * 1000
        )
        tokenRepository.save(newRefreshToken)
        
        return TokenPair(accessToken, newRefreshTokenStr)
    }
}
