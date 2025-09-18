package com.glion.api.wol

import com.glion.Config
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*

fun Route.wolRoutes() {
    route("/wol") {
        authenticate("auth-jwt") {
            post("start") {
                val body = call.receive<String>()
                // TODO : body base64 디코딩
                // TODO : body 를 AES 키로 복호화 필요
                // TODO : 복호화 한 Data Class DeSerialization
                // TODO : 맥주소 추출
                val testMac = Config.TEST_MAC
                val session = getSession()
                if(session != null) {
                    requestWol(session, testMac)
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
suspend fun requestWol(sess: String, mac: String) {
    val client = HttpClient(CIO)
    val param = "act=wakeup&mac=${mac.encodeURLParameter()}"
    try {
        val response: HttpResponse = client.get("http://${Config.wolIp}/sess-bin/wol_apply.cgi?$param") {
            header("Cookie", "efm_session_id=$sess")
            header("Referer", "http://${Config.wolIp}")
            header("Host", Config.wolIp)
            header("Connection", "Keep-Alive")
            header("User-Agent", "Apache-HttpClient/UNAVAILABLE (java 1.4)")
        }

        println("응답 코드 : ${response.status}")
        println("응답 바디 : ${response.bodyAsText()}")
    } catch(e: Exception) {

    } finally {
        client.close()
    }
}