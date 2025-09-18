package com.glion.api.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class TokenRespond(
    val value: String
)
