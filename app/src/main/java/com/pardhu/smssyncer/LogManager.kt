package com.pardhu.smssyncer

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

/** Manages error logs for debugging purposes */
object LogManager {
    private const val PREFS_NAME = "sms_syncer_logs"
    private const val KEY_LOGS = "error_logs"
    private const val MAX_LOGS = 1000
    
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val message: String,
        val details: String?
    )
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /** Add a log entry */
    fun addLog(context: Context, level: String, message: String, details: String? = null) {
        try {
            val prefs = getPrefs(context)
            val logsJson = prefs.getString(KEY_LOGS, "[]") ?: "[]"
            val logsArray = JSONArray(logsJson)
            
            // Create new log entry
            val logEntry = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("level", level)
                put("message", message)
                if (details != null) {
                    put("details", details)
                }
            }
            
            // Add to beginning of array
            val newLogsArray = JSONArray()
            newLogsArray.put(logEntry)
            
            // Copy existing logs (up to MAX_LOGS - 1)
            val limit = minOf(logsArray.length(), MAX_LOGS - 1)
            for (i in 0 until limit) {
                newLogsArray.put(logsArray.getJSONObject(i))
            }
            
            // Save back to preferences
            prefs.edit().putString(KEY_LOGS, newLogsArray.toString()).apply()
        } catch (e: Exception) {
            // Silently fail to avoid infinite loop
        }
    }
    
    /** Get all log entries */
    fun getLogs(context: Context): List<LogEntry> {
        return try {
            val prefs = getPrefs(context)
            val logsJson = prefs.getString(KEY_LOGS, "[]") ?: "[]"
            val logsArray = JSONArray(logsJson)
            
            val logs = mutableListOf<LogEntry>()
            for (i in 0 until logsArray.length()) {
                val logObj = logsArray.getJSONObject(i)
                logs.add(LogEntry(
                    timestamp = logObj.getLong("timestamp"),
                    level = logObj.getString("level"),
                    message = logObj.getString("message"),
                    details = logObj.optString("details", null)
                ))
            }
            logs
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /** Clear all logs */
    fun clearLogs(context: Context) {
        try {
            getPrefs(context).edit { remove(KEY_LOGS) }
        } catch (e: Exception) {
            // Silently fail
        }
    }
    
    /** Format timestamp for display */
    fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown time"
        }
    }
    
    /** Log levels */
    const val LEVEL_ERROR = "ERROR"
    const val LEVEL_WARNING = "WARNING"
    const val LEVEL_INFO = "INFO"
}
