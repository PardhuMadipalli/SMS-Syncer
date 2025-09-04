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

  // Notification constants
  private const val NOTIFICATION_CHANNEL_ID = "sms_syncer_channel"
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

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val channel =
                  NotificationChannel(
                                  NOTIFICATION_CHANNEL_ID,
                                  "SMS Syncer Notifications",
                                  NotificationManager.IMPORTANCE_DEFAULT
                          )
                          .apply { description = "Notifications for SMS forwarding status" }
          notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification tap action
        val intent =
                Intent(appContext, MainActivity::class.java).apply {
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
                NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_sms_mono)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
