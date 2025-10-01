package com.martovetskiy.ktorsample.data.repository

import com.martovetskiy.ktorsample.data.db.DatabaseFactory.dbQuery
import com.martovetskiy.ktorsample.data.db.tables.RefreshTokensTable
import com.martovetskiy.ktorsample.domain.model.RefreshToken
import com.martovetskiy.ktorsample.domain.repository.TokenRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ExposedTokenRepository : TokenRepository {
    
    private fun resultRowToToken(row: ResultRow) = RefreshToken(
        id = row[RefreshTokensTable.id],
        userId = row[RefreshTokensTable.userId],
        token = row[RefreshTokensTable.token],
        expiresAt = row[RefreshTokensTable.expiresAt]
    )
    
    override suspend fun save(token: RefreshToken): RefreshToken = dbQuery {
        val id = RefreshTokensTable.insert {
            it[userId] = token.userId
            it[RefreshTokensTable.token] = token.token
            it[expiresAt] = token.expiresAt
        }[RefreshTokensTable.id]
        
        token.copy(id = id)
    }
    
    override suspend fun findByToken(token: String): RefreshToken? = dbQuery {
        RefreshTokensTable.select { RefreshTokensTable.token eq token }
            .map(::resultRowToToken)
            .singleOrNull()
    }
    
    override suspend fun revokeById(id: Long): Unit = dbQuery {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.id eq id }
    }
    
    override suspend fun revokeAllForUser(userId: Long): Unit = dbQuery {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.userId eq userId }
    }
}
