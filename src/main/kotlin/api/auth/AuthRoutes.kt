package com.glion.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.glion.Config
import com.glion.api.auth.data.AesRequest
import com.glion.api.auth.data.AesRespond
import com.glion.api.auth.data.TokenRequest
import com.glion.api.auth.data.TokenRespond
import com.glion.extension.toB64DecodeByteArray
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.util.*

fun Route.authRoutes() {
    route("/auth") {
        post("/exchangeKey") {
            val request = call.receive<AesRequest>()
            // body 의 공개키 base64 로 decode
            val decodedRsaPublicKey = request.base64EncodedRsaPublicKey.toB64DecodeByteArray()

            RsaCryptoUtil.saveRsaPublicKey(decodedRsaPublicKey)
            // AES 키 생성
            AesCryptUtil.generateAesKey()
            if(AesCryptUtil.key == null) throw Exception("AES 키가 없습니다")
            val encryptedB64AesKey = RsaCryptoUtil.encrypt(AesCryptUtil.key!!).run { this.encodeBase64() }

            call.respond(AesRespond(value = encryptedB64AesKey))
        }

        /**
         * JWT 토큰 발급 & 전달
         */
        post("/getToken") {
            val request = call.receive<TokenRequest>()
            val encAppKey = request.appKey.decodeBase64Bytes()
            val iv = request.iv.decodeBase64Bytes()
            val originAppKey = AesCryptUtil.decryptAes(encAppKey, iv)
            if(originAppKey != Config.appKey) {
                call.respondText("잘못된 인증요청", status = HttpStatusCode.Unauthorized)
                return@post
            }
            val token = generateJwt(originAppKey)
            call.respond(TokenRespond(value = token))
        }

        /**
         * 토큰 갱신 API
         */
        post("/refreshToken") {
            val verifier = JWT.require(Algorithm.HMAC256(Config.jwtSecret)).withIssuer("dev-server").build()

            // 1. 헤더에서 "Bearer " 접두사를 제외한 토큰 문자열을 직접 추출합니다.
            val token = call.request.authorization()?.substringAfter("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing Token")
                return@post
            }

            try {
                // 2. verifier를 사용하여 토큰을 검증합니다.
                //    - 토큰이 유효하면 아무 일도 일어나지 않습니다.
                //    - 토큰 서명이 위조되었으면 SignatureVerificationException이 발생합니다.
                //    - 토큰이 만료되었으면 TokenExpiredException이 발생합니다.
                verifier.verify(token)
                // 만약 이 라인까지 코드가 실행된다면, 토큰이 아직 만료되지 않았다는 의미입니다.
                // 이 경우에도 새 토큰을 발급해 줄 수 있습니다.
            } catch (e: TokenExpiredException) {
                // 3. (가장 중요) 토큰이 '만료'된 것이 확실하므로, 이 예외를 정상적인 흐름으로 간주합니다.
                //    (아무것도 할 필요 없이 catch 블록을 통과)
            } catch (e: Exception) {
                // 4. 서명 불일치 등 다른 모든 예외는 '잘못된 토큰'으로 간주하여 401 에러를 응답합니다.
                call.respond(HttpStatusCode.Unauthorized, "Invalid Token: ${e.message}")
                return@post
            }

            val decodedJWT = JWT.decode(token)
            val appKey = decodedJWT.getClaim("appKey").asString()

            if (appKey == null || appKey != Config.appKey) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid appKey in Token")
                return@post
            }
            val newToken = generateJwt(appKey)
            call.respond(TokenRespond(value = newToken))
        }

        // 암호화된 값을 넘겨줄떄, 클라이언트에도 iv 값을 함께 넘겨주어야 복호화가 가능함
        // 반대로 클라이언트에서 암호화된 값을 넘겨줄때도 클라이언트에서 생성한 iv 값을 함께 넘겨주어야 서버에서 복호화가 가능함
        get("/AesTest") {
            // test
            AesCryptUtil.generateAesKey()
            println("생성된 Key :: key - ${AesCryptUtil.key}")
            try {
                val testOrigin = "abcdefghijklmnopqrstuvwxyz"
                println("암복호화 테스트 - 테스트 문자열 :: $testOrigin")
                val encryptedPair = AesCryptUtil.encryptAes(testOrigin)
                println("암호화한 텍스트 :: ${encryptedPair.first}, 암호화 시 사용된 iv :: ${encryptedPair.second}")
                val decrypted = AesCryptUtil.decryptAes(encryptedPair.first, encryptedPair.second)
                println("복호화한 텍스트 :: $decrypted")
                call.respondText("Success")
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
        .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 만료 시간 10분 설정
        .sign(Algorithm.HMAC256(Config.jwtSecret))
}