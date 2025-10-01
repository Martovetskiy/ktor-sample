package com.martovetskiy.ktorsample.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(val message: String, val username: String)

fun Route.protectedRoutes() {
    authenticate("auth-jwt") {
        route("/kotr") {
            get("/ping") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: "unknown"
                call.respond(HttpStatusCode.OK, PingResponse("pong", username))
            }
        }
    }
}
