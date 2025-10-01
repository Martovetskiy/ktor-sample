package com.martovetskiy.ktorsample.domain.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.martovetskiy.ktorsample.domain.repository.UserRepository
import com.martovetskiy.ktorsample.domain.security.JwtService
import com.martovetskiy.ktorsample.domain.security.TokenPair

class LoginUser(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    suspend fun execute(username: String, password: String): TokenPair {
        // Find user
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("Invalid credentials")
        
        // Verify password
        val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
        if (!result.verified) {
            throw IllegalArgumentException("Invalid credentials")
        }
        
        // Issue tokens
        return jwtService.issueTokensFor(user)
    }
}
