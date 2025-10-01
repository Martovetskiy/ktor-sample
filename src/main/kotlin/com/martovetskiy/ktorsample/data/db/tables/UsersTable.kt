package com.martovetskiy.ktorsample.data.db.tables

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    
    override val primaryKey = PrimaryKey(id)
}
