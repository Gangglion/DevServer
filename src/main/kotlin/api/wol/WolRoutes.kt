package com.glion.api.wol

import com.glion.Config
import com.glion.api.auth.AesCryptUtil
import com.glion.api.wol.data.WolStartRequest
import com.glion.api.wol.data.WolStartRespond
import com.glion.extension.toB64DecodeByteArray
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.wolRoutes() {
    route("/wol") {
        authenticate("auth-jwt") {
            post("start") {
                val request = call.receive<WolStartRequest>()
                val encryptedMac = request.encryptedData.toB64DecodeByteArray()
                val iv = request.iv.toB64DecodeByteArray()
                val decryptedMac = AesCryptUtil.decryptAes(encryptedMac, iv)

                // 공유기 로그인
                val session = getSession()
                if(session != null) {
                    try {
                        // 세선 정보와 맥 주소로 WOL 기능 수행
                        val result = requestWol(session, decryptedMac)
                        call.respond(
                            WolStartRespond(result = result, message = "Success")
                        )
                    } catch(e: Exception) {
                        e.printStackTrace()
                        call.respond(
                            WolStartRespond(result = false, message = e.message ?: "Wol Fail in Server")
                        )
                    }
                }
            }
        }
    }
}

/**
 * 세션 얻기
 */
suspend fun getSession(): String? {
    val client = HttpClient(CIO)
    try {
        val body = "username=${Config.username}&passwd=${Config.passwd}&act=session_id"
        val response: HttpResponse = client.post("http://${Config.wolIp}/sess-bin/login_handler.cgi") {
            header("Referer", "http://${Config.wolIp}")
            header("Content-Length", body.length)
            header("Content-Type", "application/x-www-form-urlencoded")
            header("Host", Config.wolIp)
            header("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)")
            setBody(body)
        }

        val session = response.bodyAsText()

        return session
    } catch(e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        client.close()
    }
}

/**
 * 세션 사용하여 WOL 요청
 */
suspend fun requestWol(sess: String, mac: String) : Boolean {
    val client = HttpClient(CIO)
    val param = "act=wakeup&mac=${mac.encodeURLParameter()}"
    val response: HttpResponse = client.get("http://${Config.wolIp}/sess-bin/wol_apply.cgi?$param") {
        header("Cookie", "efm_session_id=$sess")
        header("Referer", "http://${Config.wolIp}")
        header("Host", Config.wolIp)
        header("Connection", "Keep-Alive")
        header("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)")
    }

    println("응답 코드 : ${response.status}")
    println("응답 바디 : ${response.bodyAsText()}")

    client.close()

    return response.status.value == 200
}