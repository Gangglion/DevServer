package com.glion

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureJWT() {
    val jwtSecret = Config.jwtSecret
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "dev-server"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer("dev-server")
                    .build()
            )
            validate { credential ->
                if(credential.payload.getClaim("iss").asString() == "dev-server")
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}