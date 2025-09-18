package com.glion.api.fcm

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.fcmRoutes() {
    route("/fcm") {
        post("/sendPush") {
            call.respondText("FCM 으로 푸시 메시지 보낼 API 입니다.")
        }
    }
}
