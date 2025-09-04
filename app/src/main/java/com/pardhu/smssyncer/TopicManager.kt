package com.pardhu.smssyncer

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException

/** Manages secure storage and retrieval of the ntfy topic name */
object TopicManager {
    private const val PREF_NAME = "secure_topic_prefs"
    private const val KEY_TOPIC = "ntfy_topic"
    
    /**
     * Stores the topic name securely using EncryptedSharedPreferences
     */
    fun saveTopic(context: Context, topic: String): Boolean {
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
            
            sharedPreferences.edit().putString(KEY_TOPIC, topic).apply()
            true
        } catch (e: GeneralSecurityException) {
            false
        }
    }
    
    /**
     * Retrieves the stored topic name securely
     */
    fun getTopic(context: Context): String? {
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
            
            sharedPreferences.getString(KEY_TOPIC, null)
        } catch (e: GeneralSecurityException) {
            null
        }
    }
    
    /**
     * Checks if a topic is configured
     */
    fun isTopicConfigured(context: Context): Boolean {
        return getTopic(context)?.isNotEmpty() == true
    }
    
    /**
     * Masks the topic for display (shows only first 2 and last 2 characters)
     */
    fun getMaskedTopic(topic: String): String {
        return if (topic.length <= 4) {
            "*".repeat(topic.length)
        } else {
            "${topic.take(2)}${"*".repeat(topic.length - 4)}${topic.takeLast(2)}"
        }
    }
    
    /**
     * Clears the stored topic
     */
    fun clearTopic(context: Context): Boolean {
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
            
            sharedPreferences.edit().remove(KEY_TOPIC).apply()
            true
        } catch (e: GeneralSecurityException) {
            false
        }
    }
}
