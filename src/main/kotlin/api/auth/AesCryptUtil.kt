package com.glion.api.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesCryptUtil {
    companion object {
        const val PATH = "/home/devglion/devKey/aesKey"
        const val AESTransformation = "AES/GCM/NoPadding"
    }
    var key: ByteArray? = null
        private set

    /**
     * 클래스 생성 시 , 이미 존재하는 키 파일이 있다면 값을 가져옴
     */
    init {
        if(isExistKeyFile()) {
            loadAesKey()
        }
    }



    /**
     * 해당 경로에 키 파일 존재하는지 확인
     */
    fun isExistKeyFile(): Boolean {
        val file = File(PATH)
        return file.exists()
    }

    // AesKey 생성 및 파일 저장
    fun generateAesKey() : String {
        // 이미 키가 존재하고 있다면, 존재하고 있는 키 가져와서 리턴해줌
        if(isExistKeyFile()) {
            loadAesKey()
            return Base64.getEncoder().encodeToString(key)
        } else {
            // 키 새로 생성하여 리턴
            val keygen = KeyGenerator.getInstance("AES").apply { init(256) }
            val secretKey: SecretKey = keygen.generateKey()

            runBlocking(Dispatchers.IO) {
                saveAesKeyInShared(secretKey.encoded)
            }

            key = secretKey.encoded

            return Base64.getEncoder().encodeToString(secretKey.encoded)
        }
    }

    /**
     * 생성한 AesKey 파일에 저장. 각 값을 사용할때는
     */
    fun saveAesKeyInShared(key: ByteArray) {
        val file = File(PATH)
        val parentDir = file.parentFile
        if(parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }
        file.outputStream().use { out ->
            out.write(key)
        }
    }

    /**
     * 저장된 AesKey 파일 가져와 aesKey 에 저장
     */
    fun loadAesKey() {
        val file = File(PATH)
        try {
            if(!file.exists()) throw Exception("AesKey 파일이 존재하지 않습니다.")
            file.inputStream().use { input ->
                val keyBytes = input.readAllBytes()
                key = keyBytes
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * iv 값 생성 - AES/GCM 방식은 iv 를 암호화 할때마다 생성함
     */
    private fun generateIv(): ByteArray = ByteArray(12).also { SecureRandom().nextBytes(it) }

    /**
     *  AES 키로 암호화
     */
    fun encryptAes(origin: String) : Pair<ByteArray, ByteArray> {
        if(key == null) throw Exception("No AesKey File.")
        val cipher = Cipher.getInstance(AESTransformation)
        val secretKeySpec = SecretKeySpec(key, "AES")
        val iv = generateIv()
        val gcmParameterSpec = GCMParameterSpec(128, iv) // 128 bit 인증 태그
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec)
        return cipher.doFinal(origin.toByteArray(Charsets.UTF_8)) to iv
    }

    /**
     * AES 키로 복호화
     */
    fun decryptAes(crypted: ByteArray, iv: ByteArray) : String {
        if(key == null) throw Exception("No AesKey File")
        val cipher = Cipher.getInstance(AESTransformation)
        val secretKeySpec = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv) // 128 bit 인증 태그
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)
        val decrypted = cipher.doFinal(crypted)
        return String(decrypted, Charsets.UTF_8)
    }
}