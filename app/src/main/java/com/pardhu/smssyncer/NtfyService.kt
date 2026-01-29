package com.pardhu.smssyncer

import android.content.Context
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

/** Handles communication with ntfy.sh service */
object NtfyService {

  // Security: Topic is stored securely and retrieved dynamically

  /** Sends an SMS message to ntfy.sh with encryption */
  fun sendToNtfy(context: Context, displayName: String?, message: String?) {
    try {
      val topic = TopicManager.getTopic(context)
      if (topic.isNullOrEmpty()) {
        // Topic not configured, cannot send message
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
        // Password not configured, cannot encrypt message
        LogManager.addLog(
          context,
          LogManager.LEVEL_WARNING,
          "Password not configured",
          "Cannot send SMS - encryption password is not configured"
        )
        return
      }
      
      // Sanitize display name early for use in error logging
      val sanitizedDisplayName = displayName?.take(50) ?: "Unknown"
    
      val executor = Executors.newSingleThreadExecutor()
      executor.execute {
        var connection: HttpsURLConnection? = null
        try {
          val ntfyUrl = "https://ntfy.sh/$topic"
          val url = URL(ntfyUrl)
          connection = url.openConnection() as HttpsURLConnection

          connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "text/plain")
            setRequestProperty("User-Agent", "SMSSyncer/1.0")
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
          }

          // Create payload with sanitized data
          val sanitizedMessage = (message?.take(500) ?: "").trimIndent()
          val title = "SMS from $sanitizedDisplayName"

          connection.setRequestProperty("Title", title)

          // Get user-defined device name from System Settings with Android version compatibility
          val deviceName = try {
            // Try different methods based on Android version and availability
            var deviceNameFromSettings: String? = null
            
            // Method 1: Try bluetooth_name (most common user-defined name)
            try {
              deviceNameFromSettings = android.provider.Settings.Secure.getString(
                context.contentResolver, 
                "bluetooth_name"
              )
            } catch (e: Exception) {
              // Settings.Secure might not be available on older versions
            }
            
            // Method 2: Try device_name from Global settings (Android 4.2+)
            if (deviceNameFromSettings.isNullOrEmpty()) {
              try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                  deviceNameFromSettings = android.provider.Settings.Global.getString(
                    context.contentResolver, 
                    "device_name"
                  )
                }
              } catch (e: Exception) {
                // Settings.Global might not be available
              }
            }
            
            // Method 3: Try system property (older Android versions)
            if (deviceNameFromSettings.isNullOrEmpty()) {
              try {
                deviceNameFromSettings = System.getProperty("ro.product.device")
              } catch (e: Exception) {
                // System properties might not be accessible
              }
            }
            
            // Method 4: Try Build.DEVICE (hardware device name)
            if (deviceNameFromSettings.isNullOrEmpty()) {
              deviceNameFromSettings = android.os.Build.DEVICE
            }
            
            // Final fallback to Build.MODEL
            deviceNameFromSettings ?: android.os.Build.MODEL ?: "Unknown Device"
            
          } catch (e: Exception) {
            // Ultimate fallback to model name if all methods fail
            android.os.Build.MODEL ?: "Unknown Device"
          }
          
          // Encrypt the message with device name
          val messageToEncrypt = "$sanitizedDisplayName|$sanitizedMessage|$deviceName"
          val encryptedMessage = MessageEncryption.encrypt(messageToEncrypt, password)
          
          if (encryptedMessage == null) {
            // Encryption failed
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
            return@execute
          }

          // Send encrypted request
          connection.outputStream.use { outputStream ->
            val input = encryptedMessage.toByteArray(StandardCharsets.UTF_8)
            outputStream.write(input)
          }

          // Check response
          if (connection.responseCode == 200) {
            // Show android notification that message was sent
            NotificationHelper.showNotification(
                    true,
                    "SMS forwarded successfully",
                    "Message from $sanitizedDisplayName sent to laptop"
            )
          } else {
            // Log failed response
            LogManager.addLog(
              context,
              LogManager.LEVEL_ERROR,
              "SMS forwarding failed",
              "HTTP ${connection.responseCode}: Failed to send message from $sanitizedDisplayName to ntfy.sh"
            )
            // Show android notification that message was not sent
            NotificationHelper.showNotification(
                    false,
                    "SMS forwarding failed",
                    "Failed to send message from $sanitizedDisplayName"
            )
          }
        } catch (e: Exception) {
          // Log the error with details
          LogManager.addLog(
            context,
            LogManager.LEVEL_ERROR,
            "SMS forwarding error",
            "Error sending message from $sanitizedDisplayName: ${e.javaClass.simpleName} - ${e.message}"
          )
          // Show android notification that message was not sent due to error
          NotificationHelper.showNotification(
                  false,
                  "SMS forwarding error",
                  "Error sending message from $sanitizedDisplayName"
          )
        } finally {
          connection?.disconnect()
          executor.shutdown()
        }
      }
    } catch (e: Exception) {
      // Handle any errors in the main function
      LogManager.addLog(
        context,
        LogManager.LEVEL_ERROR,
        "NtfyService error",
        "Unexpected error: ${e.javaClass.simpleName} - ${e.message}"
      )
    }
  }
}
