package com.pardhu.smssyncer

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Manages encryption password with secure storage */
object PasswordManager {
    private const val PREF_NAME = "secure_password_prefs"
    private const val KEY_PASSWORD = "encryption_password"
    
    /**
     * Stores the encryption password securely using EncryptedSharedPreferences
     */
    fun savePassword(context: Context, password: String): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            sharedPreferences.edit().putString(KEY_PASSWORD, password).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves the stored encryption password
     */
    fun getPassword(context: Context): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            sharedPreferences.getString(KEY_PASSWORD, null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Checks if a password is configured
     */
    fun isPasswordConfigured(context: Context): Boolean {
        return getPassword(context)?.isNotEmpty() == true
    }
    
    /**
     * Clears the stored password
     */
    fun clearPassword(context: Context): Boolean {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            sharedPreferences.edit().remove(KEY_PASSWORD).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}
