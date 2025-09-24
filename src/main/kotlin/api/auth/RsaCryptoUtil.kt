package com.glion.api.auth

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RsaCryptoUtil {
    private const val PATH = "/home/devglion/devKey/rsaPublicKey"
    private const val RSATransformation = "RSA/ECB/PKCS1Padding"

    fun saveRsaPublicKey(rsaPublicKey: ByteArray) {
        val path = Paths.get(PATH)
        Files.write(path, rsaPublicKey)
    }

    private fun isExistRsaKey() : Boolean = File(PATH).exists()

    /**
     * 저장된 RSA 공개키를 사용하여 암호화
     */
    fun encrypt(origin: ByteArray) : ByteArray {
        if(!isExistRsaKey()) {
            throw Exception("RSA 키가 없습니다!")
        }
        val keyBytes = Files.readAllBytes(Paths.get(PATH))
        val spec = X509EncodedKeySpec(keyBytes)
        val key = KeyFactory.getInstance("RSA").generatePublic(spec)

        val cipher = Cipher.getInstance(RSATransformation)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(origin)
    }
}