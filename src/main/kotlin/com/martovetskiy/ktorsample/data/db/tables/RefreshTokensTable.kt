package com.martovetskiy.ktorsample.data.db.tables

import org.jetbrains.exposed.sql.Table

object RefreshTokensTable : Table("refresh_tokens") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UsersTable.id)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = long("expires_at")
    
    override val primaryKey = PrimaryKey(id)
}
