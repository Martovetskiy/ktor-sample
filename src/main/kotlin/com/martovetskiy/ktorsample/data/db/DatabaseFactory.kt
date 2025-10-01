package com.martovetskiy.ktorsample.data.db

import com.martovetskiy.ktorsample.data.db.tables.RefreshTokensTable
import com.martovetskiy.ktorsample.data.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(jdbcUrl: String, driverClassName: String, maxPoolSize: Int) {
        val dataSource = hikari(jdbcUrl, driverClassName, maxPoolSize)
        Database.connect(dataSource)
        
        transaction {
            SchemaUtils.create(UsersTable, RefreshTokensTable)
        }
    }
    
    private fun hikari(jdbcUrl: String, driverClassName: String, maxPoolSize: Int): HikariDataSource {
        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcUrl
            maximumPoolSize = maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
