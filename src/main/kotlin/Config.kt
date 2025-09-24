package com.glion

import io.github.cdimascio.dotenv.dotenv

object Config {
    private val dotenv = dotenv()
    val wolIp = dotenv["IPTIME_IP"] ?: ""
    val username = dotenv["IPTIME_USER_NAME"] ?: ""
    val passwd = dotenv["IPTIME_USER_PASSWD"] ?: ""

    val jwtSecret = dotenv["JWT_SECRET"] ?: ""
    val appKey = dotenv["APP_KEY"] ?: ""
}