package com.martovetskiy.ktorsample.presentation.routes

import com.martovetskiy.ktorsample.domain.usecase.LoginUser
import com.martovetskiy.ktorsample.domain.usecase.RefreshAccessToken
import com.martovetskiy.ktorsample.domain.usecase.RegisterUser
import com.martovetskiy.ktorsample.presentation.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    registerUser: RegisterUser,
    loginUser: LoginUser,
    refreshAccessToken: RefreshAccessToken
) {
    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val tokens = registerUser.execute(request.username, request.password)
                call.respond(
                    HttpStatusCode.Created,
                    TokenResponse(tokens.accessToken, tokens.refreshToken)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(e.message ?: "Registration failed")
                )
            }
        }
        
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val tokens = loginUser.execute(request.username, request.password)
                call.respond(
                    HttpStatusCode.OK,
                    TokenResponse(tokens.accessToken, tokens.refreshToken)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(e.message ?: "Login failed")
                )
            }
        }
        
        post("/refresh") {
            try {
                val request = call.receive<RefreshRequest>()
                val tokens = refreshAccessToken.execute(request.refreshToken)
                call.respond(
                    HttpStatusCode.OK,
                    TokenResponse(tokens.accessToken, tokens.refreshToken)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(e.message ?: "Token refresh failed")
                )
            }
        }
    }
}
