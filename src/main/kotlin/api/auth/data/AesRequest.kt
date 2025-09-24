package com.glion.api.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class AesRequest(
    val base64EncodedRsaPublicKey: String
)
