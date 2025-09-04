package com.pardhu.smssyncer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage

/** BroadcastReceiver for handling incoming SMS messages */
class SmsReceiver : BroadcastReceiver() {

  companion object {
    private const val TAG = "SMSSyncer"
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    try {
      // Check if context is valid
      if (context == null) return
      
      val bundle = intent?.extras ?: return
      @Suppress("DEPRECATION") val pdus = bundle.get("pdus") as? Array<*> ?: return

      for (pdu in pdus) {
        try {
          val format = bundle.getString("format")
          val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)

          val sender = sms.originatingAddress
          val message = sms.messageBody

          // Process the SMS message
          processSmsMessage(context, sender, message)
        } catch (e: Exception) {
          // Handle individual SMS processing errors
          // Don't crash the entire receiver for one bad SMS
        }
      }
    } catch (e: Exception) {
      // Security: Don't expose sensitive information in logs
      // Log.e(TAG, "Error processing SMS", e)
    }
  }

  /** Processes an SMS message and forwards it if it meets the criteria */
  private fun processSmsMessage(context: Context?, sender: String?, message: String?) {
    if (context != null && SmsFilter.shouldSendNotification(context, sender, message)) {
      // Get display name (contact name if available, otherwise phone number)
      val displayName = ContactResolver.getDisplayName(context, sender)
      NtfyService.sendToNtfy(context, displayName, message)
    }
  }
}
