package com.glion

import io.github.cdimascio.dotenv.dotenv

object Config {
    private val dotenv = dotenv()
    val wolIp = dotenv["IPTIME_IP"] ?: ""
    val username = dotenv["IPTIME_USER_NAME"] ?: ""
    val passwd = dotenv["IPTIME_USER_PASSWD"] ?: ""

    // Test
    val TEST_MAC=dotenv[""] ?: ""
}