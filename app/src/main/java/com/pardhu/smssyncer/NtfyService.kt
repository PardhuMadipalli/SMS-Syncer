package com.pardhu.smssyncer

import android.content.Context
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

/** Handles communication with ntfy.sh service */
object NtfyService {

  // Security: Topic is stored securely and retrieved dynamically

  /** Sends an SMS message to ntfy.sh */
  fun sendToNtfy(context: Context, displayName: String?, message: String?) {
    try {
      val topic = TopicManager.getTopic(context)
      if (topic.isNullOrEmpty()) {
        // Topic not configured, cannot send message
        return
      }
    
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
          val sanitizedDisplayName = displayName?.take(50) ?: "Unknown"
          val sanitizedMessage = (message?.take(500) ?: "").trimIndent()
          val title = "SMS from $sanitizedDisplayName"

          connection.setRequestProperty("Title", title)

          // Send request
          connection.outputStream.use { outputStream ->
            val input = sanitizedMessage.toByteArray(StandardCharsets.UTF_8)
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
            // Show android notification that message was not sent
            NotificationHelper.showNotification(
                    false,
                    "SMS forwarding failed",
                    "Failed to send message from $sanitizedDisplayName"
            )
          }
        } catch (e: Exception) {
          // Show android notification that message was not sent due to error
          val sanitizedDisplayName = displayName?.take(50) ?: "Unknown"
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
    }
  }
}
