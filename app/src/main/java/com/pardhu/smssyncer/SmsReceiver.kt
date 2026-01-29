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
      if (context == null) {
        return
      }
      
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
          // Log individual SMS processing errors
          LogManager.addLog(
            context,
            LogManager.LEVEL_ERROR,
            "Failed to process SMS",
            "Error: ${e.javaClass.simpleName} - ${e.message}"
          )
        }
      }
    } catch (e: Exception) {
      // Log receiver errors
      context?.let {
        LogManager.addLog(
          it,
          LogManager.LEVEL_ERROR,
          "SMS Receiver error",
          "Error: ${e.javaClass.simpleName} - ${e.message}"
        )
      }
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
