package com.pardhu.smssyncer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Manages app permissions for SMS and notifications */
object PermissionManager {

  /** Checks if all required permissions are granted */
  fun areAllPermissionsGranted(context: Context): Boolean {
    val receiveSmsPermission = context.checkSelfPermission(Manifest.permission.RECEIVE_SMS)
    val readSmsPermission = context.checkSelfPermission(Manifest.permission.READ_SMS)
    val readContactsPermission = context.checkSelfPermission(Manifest.permission.READ_CONTACTS)
    val notificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else {
              PackageManager
                      .PERMISSION_GRANTED // Pre-Android 13, notifications are granted by default
            }

    return receiveSmsPermission == PackageManager.PERMISSION_GRANTED &&
            readSmsPermission == PackageManager.PERMISSION_GRANTED &&
            readContactsPermission == PackageManager.PERMISSION_GRANTED &&
            notificationPermission == PackageManager.PERMISSION_GRANTED
  }

  /** Gets the list of permissions that need to be requested */
  fun getPermissionsToRequest(context: Context): Array<String> {
    val permissionsToRequest = mutableListOf<String>()

    // Check SMS permissions
    if (context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) !=
                    PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
    }
    if (context.checkSelfPermission(Manifest.permission.READ_SMS) !=
                    PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.READ_SMS)
    }

    // Check contacts permission
    if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED
    ) {
      permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
    }

    // Check notification permission (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                      PackageManager.PERMISSION_GRANTED
      ) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    return permissionsToRequest.toTypedArray()
  }

  /** Checks if a specific permission is granted */
  fun isPermissionGranted(context: Context, permission: String): Boolean {
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
  }
}
