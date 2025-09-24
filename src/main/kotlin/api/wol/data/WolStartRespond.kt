package com.glion.api.wol.data

import kotlinx.serialization.Serializable

@Serializable
data class WolStartRespond(
    val result: Boolean,
    val message: String = ""
)