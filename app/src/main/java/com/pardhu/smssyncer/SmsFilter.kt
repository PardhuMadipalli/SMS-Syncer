package com.pardhu.smssyncer

import android.content.Context
import java.util.regex.Pattern

/** Handles SMS filtering logic to determine which messages should be forwarded */
object SmsFilter {

  /** Determines if an SMS should be forwarded based on sender and message content */
  fun shouldSendNotification(context: Context, sender: String?, message: String?): Boolean {
    if (sender.isNullOrEmpty() || message.isNullOrEmpty()) return false

    val senderLower = sender.lowercase()
    val messageLower = message.lowercase()

    // Check if forward all is enabled
    if (FilterManager.getForwardAll(context)) {
      return true
    }

    // Check for important senders
    if (isImportantSender(context, senderLower)) {
      return true
    }

    // Check for important keywords
    if (containsImportantKeywords(context, messageLower)) {
      return true
    }

    // Check for numeric codes (OTP, verification codes)
    if (containsNumericCode(context, message)) {
      return true
    }

    // Filter out promotional messages
    if (containsSpamKeywords(context, messageLower)) {
      return false
    }

    // Default: don't send
    return false
  }

  private fun isImportantSender(context: Context, senderLower: String): Boolean {
    val importantSenders = FilterManager.getImportantSenders(context)
    return importantSenders.any { senderLower.contains(it) }
  }

  private fun containsImportantKeywords(context: Context, messageLower: String): Boolean {
    val keywords = FilterManager.getImportantKeywords(context)
    return keywords.any { messageLower.contains(it) }
  }

  private fun containsNumericCode(context: Context, message: String): Boolean {
    val otpPattern = FilterManager.getOtpPattern(context)
    val pattern = Pattern.compile(otpPattern)
    return pattern.matcher(message).find()
  }

  private fun containsSpamKeywords(context: Context, messageLower: String): Boolean {
    val spamKeywords = FilterManager.getSpamKeywords(context)
    return spamKeywords.any { messageLower.contains(it) }
  }
}
