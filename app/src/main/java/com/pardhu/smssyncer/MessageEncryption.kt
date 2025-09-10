package com.pardhu.smssyncer

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Handles encryption/decryption of SMS messages for secure transmission */
object MessageEncryption {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val CHARSET = "UTF-8"
    
    /**
     * Encrypts a message using the provided password
     * Returns base64 encoded string in format: IV:ENCRYPTED_DATA
     */
    fun encrypt(message: String, password: String): String? {
        return try {
            // Create a 32-byte key from password using SHA-256
            val key = createKey(password)
            
            // Generate random IV
            val iv = ByteArray(16)
            java.security.SecureRandom().nextBytes(iv)
            
            // Create cipher
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(key, ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            
            // Encrypt the message
            val encrypted = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data, then base64 encode
            val combined = iv + encrypted
            val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            
            encoded
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypts a message using the provided password
     * Expects base64 encoded string in format: IV:ENCRYPTED_DATA
     */
    fun decrypt(encryptedMessage: String, password: String): String? {
        return try {
            // Decode base64
            val combined = Base64.decode(encryptedMessage, Base64.NO_WRAP)
            
            // Extract IV (first 16 bytes) and encrypted data
            val iv = combined.sliceArray(0..15)
            val encrypted = combined.sliceArray(16 until combined.size)
            
            // Create key from password
            val key = createKey(password)
            
            // Create cipher
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(key, ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            
            // Decrypt
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Creates a 32-byte key from password using SHA-256
     */
    private fun createKey(password: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
