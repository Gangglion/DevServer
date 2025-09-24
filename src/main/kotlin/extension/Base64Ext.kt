package com.glion.extension

import java.util.*

/**
 * ByteArray encode String(Base64) Extension Function
 */
fun ByteArray.toEncodeB64() : String =
    Base64.getEncoder().encodeToString(this)

/**
 * String decode ByteArray(base64) Extension Function
 */
fun String.toB64DecodeByteArray(): ByteArray =
    Base64.getDecoder().decode(this)

fun String.toB64DecodeStr(): String {
    val decode = Base64.getDecoder().decode(this)
    return String(decode, Charsets.UTF_8)
}