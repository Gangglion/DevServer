package com.glion

import com.glion.api.auth.AesCryptUtil
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureJWT()
    configureMonitoring()
    configureSerialization()
    configureRouting()

    // AES 키 유틸 초기화 - 키 값 가져옴
    AesCryptUtil.init()
}