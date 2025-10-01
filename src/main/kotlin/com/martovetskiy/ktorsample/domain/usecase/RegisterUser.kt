package com.martovetskiy.ktorsample.domain.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.martovetskiy.ktorsample.domain.model.User
import com.martovetskiy.ktorsample.domain.repository.UserRepository
import com.martovetskiy.ktorsample.domain.security.JwtService
import com.martovetskiy.ktorsample.domain.security.TokenPair

class RegisterUser(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    suspend fun execute(username: String, password: String): TokenPair {
        // Check if user already exists
        val existing = userRepository.findByUsername(username)
        if (existing != null) {
            throw IllegalArgumentException("User already exists")
        }
        
        // Hash password
        val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        
        // Create user
        val user = userRepository.create(
            User(username = username, passwordHash = passwordHash)
        )
        
        // Issue tokens
        return jwtService.issueTokensFor(user)
    }
}
