package com.martovetskiy.ktorsample.domain.repository

import com.martovetskiy.ktorsample.domain.model.User

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findByUsername(username: String): User?
    suspend fun create(user: User): User
}
