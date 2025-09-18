package com.glion.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.glion.Config
import com.glion.api.auth.data.TokenRequest
import com.glion.api.auth.data.TokenRespond
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.authRoutes() {
    route("/auth") {
        post("/getToken") {
            val request = call.receive<TokenRequest>()

            if(request.appKey != Config.appKey) {
                call.respondText("잘못된 인증요청", status = HttpStatusCode.Unauthorized)
                return@post
            }
            val token = generateJwt(request.appKey)
            call.respond(TokenRespond(value = token))
        }
        // 이 블럭 내의 API 는 헤더에 JWT 토큰이 필요함
        authenticate("auth-jwt") {
            /**
             * 토큰 갱신 API
             */
            post("/refreshToken") {
                val principal = call.principal<JWTPrincipal>()
                val appKey = principal?.getClaim("appKey", String::class)
                if(appKey == null || appKey != Config.appKey) {
                    call.respondText("Invaild Token", status = HttpStatusCode.Unauthorized)
                    return@post
                }
                val newToken = generateJwt(appKey)
                call.respond(TokenRespond(value = newToken))
            }

            post("/exchangeKey") {
                // TODO : body 의 공개키 base64 로 decode
                // TODO : decode 된 공개키를 키 저장 경로에 저장
                // AES 키 생성
                val aesCrypt = AesCryptUtil()
                aesCrypt.generateAesKey()
                // TODO : AesKey 를 rsaPublic.pem 으로 암호화
                // TODO : 암호화된 문자열 base64 로 인코딩
                // TODO : 클라이언트에 base64 로 인코딩된 암호화된 문자열 respond
            }
        }

        // 암호화된 값을 넘겨줄떄, 클라이언트에도 iv 값을 함께 넘겨주어야 복호화가 가능함
        // 반대로 클라이언트에서 암호화된 값을 넘겨줄때도 클라이언트에서 생성한 iv 값을 함께 넘겨주어야 서버에서 복호화가 가능함
        get("/AesTest") {
            // test
            val aesCrypt = AesCryptUtil()
            aesCrypt.generateAesKey()
            println("생성된 Key :: key - ${aesCrypt.key}")
            try {
                val testOrigin = "abcdefghijklmnopqrstuvwxyz"
                println("암복호화 테스트 - 테스트 문자열 :: $testOrigin")
                val encryptedPair = aesCrypt.encryptAes(testOrigin)
                println("암호화한 텍스트 :: ${encryptedPair.first}, 암호화 시 사용된 iv :: ${encryptedPair.second}")
                val decrypted = aesCrypt.decryptAes(encryptedPair.first, encryptedPair.second)
                println("복호화한 텍스트 :: $decrypted")
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun generateJwt(appKey: String): String {
    return JWT.create()
        .withIssuer("dev-server")
        .withClaim("appKey", appKey)
        .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 만료 시간 10분 설정
        .sign(Algorithm.HMAC256(Config.jwtSecret))
}