package com.pardhu.smssyncer

import android.content.Context
import android.content.SharedPreferences

/** Manages customizable SMS filters with standard storage */
object FilterManager {
    private const val PREF_NAME = "filter_prefs"
    private const val KEY_IMPORTANT_SENDERS = "important_senders"
    private const val KEY_IMPORTANT_KEYWORDS = "important_keywords"
    private const val KEY_SPAM_KEYWORDS = "spam_keywords"
    private const val KEY_OTP_PATTERN = "otp_pattern"
    private const val KEY_FORWARD_ALL = "forward_all"

    // Default filter values
    private val DEFAULT_IMPORTANT_SENDERS = listOf(
        "bank", "delivery", "uber", "lyft", "amazon", "paypal", "venmo", "zelle",
        "doctor", "urgent", "security", "alert", "hdfc", "icici", "sbi", "axis",
        "kotak", "swiggy", "zomato", "flipkart", "myntra", "ola"
    )

    private val DEFAULT_IMPORTANT_KEYWORDS = listOf(
        "urgent", "important", "delivery", "otp", "code", "verification", "security",
        "alert", "confirm", "expires", "deadline", "delivered", "transaction",
        "payment", "credited", "debited", "balance", "debit", "credit", "login", "log on"
    )

    private val DEFAULT_SPAM_KEYWORDS = listOf(
        "offer", "discount", "sale", "promo", "unsubscribe", "marketing",
        "advertisement", "free", "win", "prize", "cashback", "rewards", "lucky", "congratulations"
    )

    private const val DEFAULT_OTP_PATTERN = "\\b\\d{4,8}\\b"
    private const val DEFAULT_FORWARD_ALL = false

    /**
     * Gets the current important senders list
     */
    fun getImportantSenders(context: Context): List<String> {
        return try {
            val prefs = getPrefs(context)
            val stored = prefs.getString(KEY_IMPORTANT_SENDERS, null)
            if (stored != null) {
                stored.split(",").filter { it.isNotEmpty() }
            } else {
                DEFAULT_IMPORTANT_SENDERS
            }
        } catch (e: Exception) {
            DEFAULT_IMPORTANT_SENDERS
        }
    }

    /**
     * Gets the current important keywords list
     */
    fun getImportantKeywords(context: Context): List<String> {
        return try {
            val prefs = getPrefs(context)
            val stored = prefs.getString(KEY_IMPORTANT_KEYWORDS, null)
            if (stored != null) {
                stored.split(",").filter { it.isNotEmpty() }
            } else {
                DEFAULT_IMPORTANT_KEYWORDS
            }
        } catch (e: Exception) {
            DEFAULT_IMPORTANT_KEYWORDS
        }
    }

    /**
     * Gets the current spam keywords list
     */
    fun getSpamKeywords(context: Context): List<String> {
        return try {
            val prefs = getPrefs(context)
            val stored = prefs.getString(KEY_SPAM_KEYWORDS, null)
            if (stored != null) {
                stored.split(",").filter { it.isNotEmpty() }
            } else {
                DEFAULT_SPAM_KEYWORDS
            }
        } catch (e: Exception) {
            DEFAULT_SPAM_KEYWORDS
        }
    }

    /**
     * Gets the current OTP pattern
     */
    fun getOtpPattern(context: Context): String {
        return try {
            val prefs = getPrefs(context)
            prefs.getString(KEY_OTP_PATTERN, DEFAULT_OTP_PATTERN) ?: DEFAULT_OTP_PATTERN
        } catch (e: Exception) {
            DEFAULT_OTP_PATTERN
        }
    }

    /**
     * Gets whether to forward all messages
     */
    fun getForwardAll(context: Context): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.getBoolean(KEY_FORWARD_ALL, DEFAULT_FORWARD_ALL)
        } catch (e: Exception) {
            DEFAULT_FORWARD_ALL
        }
    }

    /**
     * Saves custom important senders
     */
    fun saveImportantSenders(context: Context, senders: List<String>): Boolean {
        return try {
            val prefs = getPrefs(context)
            val joined = senders.joinToString(",")
            prefs.edit().putString(KEY_IMPORTANT_SENDERS, joined).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Saves custom important keywords
     */
    fun saveImportantKeywords(context: Context, keywords: List<String>): Boolean {
        return try {
            val prefs = getPrefs(context)
            val joined = keywords.joinToString(",")
            prefs.edit().putString(KEY_IMPORTANT_KEYWORDS, joined).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Saves custom spam keywords
     */
    fun saveSpamKeywords(context: Context, keywords: List<String>): Boolean {
        return try {
            val prefs = getPrefs(context)
            val joined = keywords.joinToString(",")
            prefs.edit().putString(KEY_SPAM_KEYWORDS, joined).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Saves custom OTP pattern
     */
    fun saveOtpPattern(context: Context, pattern: String): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.edit().putString(KEY_OTP_PATTERN, pattern).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Saves forward all setting
     */
    fun saveForwardAll(context: Context, forwardAll: Boolean): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.edit().putBoolean(KEY_FORWARD_ALL, forwardAll).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Resets all filters to default values
     */
    fun resetToDefaults(context: Context): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if filters have been customized
     */
    fun areFiltersCustomized(context: Context): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.contains(KEY_IMPORTANT_SENDERS) ||
                    prefs.contains(KEY_IMPORTANT_KEYWORDS) ||
                    prefs.contains(KEY_SPAM_KEYWORDS) ||
                    prefs.contains(KEY_OTP_PATTERN) ||
                    prefs.contains(KEY_FORWARD_ALL)
        } catch (e: Exception) {
            false
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
