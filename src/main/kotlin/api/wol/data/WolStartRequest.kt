package com.glion.api.wol.data

import kotlinx.serialization.Serializable

@Serializable
data class WolStartRequest(
    val encryptedData: String,
    val iv: String
)
