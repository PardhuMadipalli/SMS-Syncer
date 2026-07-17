package com.pardhu.smssyncer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/** Handles Android notifications for SMS forwarding status */
object NotificationHelper {

  // Notification channel IDs
  // Separate channels let the user independently enable/disable success vs error
  // notifications from the Android system notification settings.
  private const val NOTIFICATION_CHANNEL_SUCCESS_ID = "sms_syncer_success_channel"
  private const val NOTIFICATION_CHANNEL_ERROR_ID = "sms_syncer_error_channel"
  // Legacy single channel from older app versions, kept only so it can be deleted.
  private const val NOTIFICATION_CHANNEL_LEGACY_ID = "sms_syncer_channel"
  private const val NOTIFICATION_SUCCESS_ID = 1001
  private const val NOTIFICATION_FAILURE_ID = 1002

  /** Shows a notification about SMS forwarding status */
  fun showNotification(isSuccess: Boolean, title: String, message: String) {
    // Use a simpler approach to get application context
    try {
      val context =
              Class.forName("android.app.ActivityThread")
                      .getMethod("currentApplication")
                      .invoke(null) as?
                      Context

      context?.let { appContext ->
        val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create both notification channels for Android 8.0+.
        // Creating them is idempotent, so it's safe to call every time.
        val successChannel =
                NotificationChannel(
                                NOTIFICATION_CHANNEL_SUCCESS_ID,
                                "Success notifications",
                                NotificationManager.IMPORTANCE_DEFAULT
                        )
                        .apply {
                          description = "Notifications when an SMS is forwarded successfully"
                        }
        val errorChannel =
                NotificationChannel(
                                NOTIFICATION_CHANNEL_ERROR_ID,
                                "Error notifications",
                                NotificationManager.IMPORTANCE_HIGH
                        )
                        .apply {
                          description = "Notifications when SMS forwarding or encryption fails"
                        }
        notificationManager.createNotificationChannel(successChannel)
        notificationManager.createNotificationChannel(errorChannel)

        // Remove the legacy single channel from older versions so it no longer
        // shows up as a stale, unused channel in the system notification settings.
        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_LEGACY_ID)

        val channelId =
                if (isSuccess) NOTIFICATION_CHANNEL_SUCCESS_ID else NOTIFICATION_CHANNEL_ERROR_ID

        // Create intent for notification tap action
        // For error notifications, open LogsActivity; for success, open MainActivity
        val targetActivity = if (isSuccess) MainActivity::class.java else LogsActivity::class.java
        val intent =
                Intent(appContext, targetActivity).apply {
                  flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        val pendingIntent =
                PendingIntent.getActivity(
                        appContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        // Build notification
        val notification =
                NotificationCompat.Builder(appContext, channelId)
                        .setSmallIcon(R.drawable.ic_notification_sms_mono)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(
                                if (isSuccess) NotificationCompat.PRIORITY_DEFAULT
                                else NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

        // Show notification
        val notificationId = if (isSuccess) NOTIFICATION_SUCCESS_ID else NOTIFICATION_FAILURE_ID
        notificationManager.notify(notificationId, notification)
      }
    } catch (e: Exception) {
      // Fallback: notification won't be shown if context can't be obtained
      // This is expected in some cases when called from static context
    }
  }
}
