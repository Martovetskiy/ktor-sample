package com.martovetskiy.ktorsample

import com.martovetskiy.ktorsample.data.db.DatabaseFactory
import com.martovetskiy.ktorsample.data.repository.ExposedTokenRepository
import com.martovetskiy.ktorsample.data.repository.ExposedUserRepository
import com.martovetskiy.ktorsample.data.security.JwtServiceImpl
import com.martovetskiy.ktorsample.domain.usecase.LoginUser
import com.martovetskiy.ktorsample.domain.usecase.RefreshAccessToken
import com.martovetskiy.ktorsample.domain.usecase.RegisterUser
import com.martovetskiy.ktorsample.presentation.routes.authRoutes
import com.martovetskiy.ktorsample.presentation.routes.protectedRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Load configuration
    val jwtSecret = environment.config.property("security.jwt.secret").getString()
    val jwtIssuer = environment.config.property("security.jwt.issuer").getString()
    val jwtAudience = environment.config.property("security.jwt.audience").getString()
    val jwtRealm = environment.config.property("security.jwt.realm").getString()
    val accessTokenTTL = environment.config.property("security.jwt.accessTokenTTL").getString().toLong()
    val refreshTokenTTL = environment.config.property("security.jwt.refreshTokenTTL").getString().toLong()
    
    val jdbcUrl = environment.config.property("database.jdbcUrl").getString()
    val driverClassName = environment.config.property("database.driverClassName").getString()
    val maxPoolSize = environment.config.property("database.maxPoolSize").getString().toInt()
    
    // Initialize database
    DatabaseFactory.init(jdbcUrl, driverClassName, maxPoolSize)
    
    // Initialize repositories
    val userRepository = ExposedUserRepository()
    val tokenRepository = ExposedTokenRepository()
    
    // Initialize services
    val jwtService = JwtServiceImpl(
        secret = jwtSecret,
        issuer = jwtIssuer,
        audience = jwtAudience,
        accessTokenTTL = accessTokenTTL,
        refreshTokenTTL = refreshTokenTTL,
        tokenRepository = tokenRepository
    )
    
    // Initialize use cases
    val registerUser = RegisterUser(userRepository, jwtService)
    val loginUser = LoginUser(userRepository, jwtService)
    val refreshAccessToken = RefreshAccessToken(jwtService)
    
    // Install plugins
    install(ContentNegotiation) {
        json()
    }
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(jwtService.verifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    
    // Setup routing
    routing {
        authRoutes(registerUser, loginUser, refreshAccessToken)
        protectedRoutes()
    }
}
