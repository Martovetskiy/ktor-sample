package com.martovetskiy.ktorsample.data.repository

import com.martovetskiy.ktorsample.data.db.DatabaseFactory.dbQuery
import com.martovetskiy.ktorsample.data.db.tables.UsersTable
import com.martovetskiy.ktorsample.domain.model.User
import com.martovetskiy.ktorsample.domain.repository.UserRepository
import org.jetbrains.exposed.sql.*

class ExposedUserRepository : UserRepository {
    
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UsersTable.id],
        username = row[UsersTable.username],
        passwordHash = row[UsersTable.passwordHash]
    )
    
    override suspend fun findById(id: Long): User? = dbQuery {
        UsersTable.select { UsersTable.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }
    
    override suspend fun findByUsername(username: String): User? = dbQuery {
        UsersTable.select { UsersTable.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }
    
    override suspend fun create(user: User): User = dbQuery {
        val id = UsersTable.insert {
            it[username] = user.username
            it[passwordHash] = user.passwordHash
        }[UsersTable.id]
        
        user.copy(id = id)
    }
}
