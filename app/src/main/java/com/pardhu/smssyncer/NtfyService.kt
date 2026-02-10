package com.pardhu.smssyncer

import android.content.Context
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

/** Handles communication with ntfy.sh service */
object NtfyService {

  // Security: Topic is stored securely and retrieved dynamically

  /** Sends an SMS message to ntfy.sh with encryption and retry logic */
  fun sendToNtfy(context: Context, displayName: String?, message: String?) {
    try {
      val topic = TopicManager.getTopic(context)
      if (topic.isNullOrEmpty()) {
        LogManager.addLog(
          context,
          LogManager.LEVEL_WARNING,
          "Topic not configured",
          "Cannot send SMS - ntfy.sh topic is not configured"
        )
        return
      }
      
      val password = PasswordManager.getPassword(context)
      if (password.isNullOrEmpty()) {
        LogManager.addLog(
          context,
          LogManager.LEVEL_WARNING,
          "Password not configured",
          "Cannot send SMS - encryption password is not configured"
        )
        return
      }
      
      val sanitizedDisplayName = displayName?.take(50) ?: "Unknown"
    
      val executor = Executors.newSingleThreadExecutor()
      executor.execute {
        var lastException: Exception? = null
        
        // Retry up to 3 times with exponential backoff
        for (attempt in 1..3) {
          try {
            if (sendMessageAttempt(context, topic, password, sanitizedDisplayName, message, attempt)) {
              executor.shutdown()
              return@execute
            }
          } catch (e: Exception) {
            lastException = e
            if (attempt < 3) {
              Thread.sleep((1000L * attempt).coerceAtMost(5000L))
            }
          }
        }
        
        // All retries failed
        val errorMsg = lastException?.message ?: "Unknown error"
        LogManager.addLog(
          context,
          LogManager.LEVEL_ERROR,
          "SMS forwarding failed after retries",
          "Failed after 3 attempts from $sanitizedDisplayName: $errorMsg"
        )
        NotificationHelper.showNotification(
          false,
          "SMS forwarding failed",
          "Failed to send message from $sanitizedDisplayName"
        )
        executor.shutdown()
      }
    } catch (e: Exception) {
      // Handle any errors in the main function
    }
  }
  
  private fun sendMessageAttempt(
    context: Context,
    topic: String,
    password: String,
    sanitizedDisplayName: String,
    message: String?,
    attempt: Int
  ): Boolean {
    var connection: HttpsURLConnection? = null
    try {
      val ntfyUrl = "https://ntfy.sh/$topic"
      val url = URL(ntfyUrl)
      connection = url.openConnection() as HttpsURLConnection

      connection.apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "text/plain")
        setRequestProperty("User-Agent", "SMSSyncer/1.0")
        setRequestProperty("Connection", "close") // Prevent connection reuse
        doOutput = true
        connectTimeout = 15000
        readTimeout = 15000
      }

      val sanitizedMessage = (message?.take(500) ?: "").trimIndent()
      val title = "SMS from $sanitizedDisplayName"
      connection.setRequestProperty("Title", title)

      // Get device name
      val deviceName = try {
        var deviceNameFromSettings: String? = null
        
        try {
          deviceNameFromSettings = android.provider.Settings.Secure.getString(
            context.contentResolver, 
            "bluetooth_name"
          )
        } catch (e: Exception) { }
        
        if (deviceNameFromSettings.isNullOrEmpty()) {
          try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
              deviceNameFromSettings = android.provider.Settings.Global.getString(
                context.contentResolver, 
                "device_name"
              )
            }
          } catch (e: Exception) { }
        }
        
        if (deviceNameFromSettings.isNullOrEmpty()) {
          try {
            deviceNameFromSettings = System.getProperty("ro.product.device")
          } catch (e: Exception) { }
        }
        
        if (deviceNameFromSettings.isNullOrEmpty()) {
          deviceNameFromSettings = android.os.Build.DEVICE
        }
        
        deviceNameFromSettings ?: android.os.Build.MODEL ?: "Unknown Device"
      } catch (e: Exception) {
        android.os.Build.MODEL ?: "Unknown Device"
      }
      
      // Encrypt the message
      val messageToEncrypt = "$sanitizedDisplayName|$sanitizedMessage|$deviceName"
      val encryptedMessage = MessageEncryption.encrypt(messageToEncrypt, password)
      
      if (encryptedMessage == null) {
        LogManager.addLog(
          context,
          LogManager.LEVEL_ERROR,
          "Message encryption failed",
          "Failed to encrypt SMS from $sanitizedDisplayName"
        )
        NotificationHelper.showNotification(
          false,
          "SMS encryption failed",
          "Failed to encrypt message from $sanitizedDisplayName"
        )
        return false
      }

      // Send encrypted request
      connection.outputStream.use { outputStream ->
        val input = encryptedMessage.toByteArray(StandardCharsets.UTF_8)
        outputStream.write(input)
        outputStream.flush()
      }

      // Read response to ensure connection completes
      val responseCode = connection.responseCode
      
      if (responseCode == 200) {
        // Consume response body to prevent connection issues
        connection.inputStream.use { it.readBytes() }
        
        NotificationHelper.showNotification(
          true,
          "SMS forwarded successfully",
          "Message from $sanitizedDisplayName sent to laptop"
        )
        return true
      } else {
        LogManager.addLog(
          context,
          LogManager.LEVEL_ERROR,
          "SMS forwarding failed",
          "HTTP $responseCode: Failed to send from $sanitizedDisplayName (attempt $attempt)"
        )
        return false
      }
    } catch (e: Exception) {
      LogManager.addLog(
        context,
        LogManager.LEVEL_ERROR,
        "SMS forwarding error",
        "Attempt $attempt from $sanitizedDisplayName: ${e.javaClass.simpleName} - ${e.message}"
      )
      throw e
    } finally {
      connection?.disconnect()
    }
  }
}
