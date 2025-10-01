package com.rednoir.domain.repository

import com.rednoir.domain.entity.Email
import com.rednoir.domain.entity.User

interface UserRepository: BaseRepository<User, Long> {
    suspend fun findByEmail(email: Email): User?
}