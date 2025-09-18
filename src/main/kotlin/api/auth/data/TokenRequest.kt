package com.glion.api.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val appKey: String
)
