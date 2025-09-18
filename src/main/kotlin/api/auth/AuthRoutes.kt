package com.glion.api.auth

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {
        post("/getToken") {
            // JWT 토큰 받기
            call.respondText("인증과 관련된 API 입니다.")
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