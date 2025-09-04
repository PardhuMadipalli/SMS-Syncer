package com.pardhu.smssyncer

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import java.util.Locale

/** Utility class to resolve phone numbers to contact names */
object ContactResolver {

  /** Resolves a phone number to a contact name if available */
  fun getContactName(context: Context, phoneNumber: String?): String? {
    if (phoneNumber.isNullOrEmpty()) return null
    
    // Check if we have contacts permission
    if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) 
        != PackageManager.PERMISSION_GRANTED) {
      return null
    }

    return try {
      val contentResolver: ContentResolver = context.contentResolver
      
      // Normalize the phone number for comparison
      val normalizedNumber = normalizePhoneNumber(phoneNumber)
      
      // Query contacts by phone number
      val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(normalizedNumber)
      )
      
      val projection = arrayOf(
        ContactsContract.PhoneLookup.DISPLAY_NAME,
        ContactsContract.PhoneLookup.NUMBER
      )
      
      val cursor: Cursor? = contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
      )
      
      cursor?.use { c ->
        if (c.moveToFirst()) {
          val displayName = c.getString(c.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
          if (!displayName.isNullOrEmpty()) {
            return displayName
          }
        }
      }
      
      // If no exact match found, try a broader search
      searchContactsByNumber(contentResolver, normalizedNumber)
      
    } catch (e: Exception) {
      // Security: Don't expose sensitive information in logs
      null
    }
  }

  /** Performs a broader search for contacts by phone number */
  private fun searchContactsByNumber(contentResolver: ContentResolver, phoneNumber: String): String? {
    return try {
      val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
      val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
      )
      
      val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
      val selectionArgs = arrayOf("%$phoneNumber%")
      
      val cursor: Cursor? = contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        null
      )
      
      cursor?.use { c ->
        while (c.moveToNext()) {
          val contactNumber = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
          val displayName = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
          
          // Check if the numbers match (normalized)
          if (PhoneNumberUtils.compare(phoneNumber, contactNumber)) {
            if (!displayName.isNullOrEmpty()) {
              return displayName
            }
          }
        }
      }
      
      null
    } catch (e: Exception) {
      null
    }
  }

  /** Normalizes a phone number for comparison */
  private fun normalizePhoneNumber(phoneNumber: String): String {
    // Remove all non-digit characters except +
    val cleaned = phoneNumber.replace(Regex("[^\\d+]"), "")
    
    // If it starts with +, keep it as is
    if (cleaned.startsWith("+")) {
      return cleaned
    }
    
    // If it's a local number, try to format it properly
    return cleaned
  }

  /** Gets the display name for SMS sender, falling back to phone number if no contact found */
  fun getDisplayName(context: Context, phoneNumber: String?): String {
    if (phoneNumber.isNullOrEmpty()) return "Unknown"
    
    val contactName = getContactName(context, phoneNumber)
    return contactName ?: phoneNumber
  }
}
