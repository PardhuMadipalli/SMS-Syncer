package com.pardhu.smssyncer

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap

/**
 * Buffers multi-part SMS messages and combines them into a single message.
 * Handles out-of-order delivery and missing parts with timeout mechanism.
 */
class SmsMessageBuffer {

  companion object {
    private const val TIMEOUT_MS = 2000L // 2 seconds timeout for collecting parts
    private const val CLEANUP_INTERVAL_MS = 4000L // Clean up old entries every 4 seconds
  }

  /** Represents a single part of a multi-part SMS */
  private data class SmsPart(
    val message: String,
    val timestamp: Long,
    val partIndex: Int = 0
  )

  /** Represents a collection of SMS parts being assembled */
  private data class SmsCollection(
    val parts: MutableList<SmsPart> = mutableListOf(),
    val firstReceivedTime: Long = System.currentTimeMillis(),
    var timeoutScheduled: Boolean = false
  )

  private val buffer = ConcurrentHashMap<String, SmsCollection>()
  private val handler = Handler(Looper.getMainLooper())
  private var cleanupScheduled = false

  /**
   * Adds an SMS part to the buffer.
   * 
   * @param sender The phone number of the sender
   * @param message The message content of this part
   * @param timestamp The timestamp when the message was received
   * @param onComplete Callback invoked when all parts are collected or timeout occurs
   */
  fun addPart(
    sender: String,
    message: String,
    timestamp: Long,
    onComplete: (String) -> Unit
  ) {
    // Create a unique key for this message group
    // Round timestamp to nearest second to group parts arriving close together
    val roundedTimestamp = (timestamp / 1000) * 1000
    val key = "$sender:$roundedTimestamp"

    synchronized(buffer) {
      val collection = buffer.getOrPut(key) { SmsCollection() }
      
      // Add this part to the collection
      collection.parts.add(SmsPart(message, timestamp))

      // Schedule timeout if not already scheduled
      if (!collection.timeoutScheduled) {
        collection.timeoutScheduled = true
        scheduleTimeout(key, onComplete)
      }

      // Schedule periodic cleanup if not already scheduled
      if (!cleanupScheduled) {
        scheduleCleanup()
      }
    }
  }

  /**
   * Schedules a timeout to process collected parts even if not all parts arrived.
   */
  private fun scheduleTimeout(key: String, onComplete: (String) -> Unit) {
    handler.postDelayed({
      synchronized(buffer) {
        val collection = buffer.remove(key)
        if (collection != null && collection.parts.isNotEmpty()) {
          val completeMessage = assembleParts(collection.parts)
          onComplete(completeMessage)
        }
      }
    }, TIMEOUT_MS)
  }

  /**
   * Assembles multiple SMS parts into a single message.
   * Sorts parts by timestamp to maintain correct order.
   */
  private fun assembleParts(parts: List<SmsPart>): String {
    return parts
      .sortedBy { it.timestamp }
      .joinToString("") { it.message }
  }

  /**
   * Schedules periodic cleanup of old buffer entries to prevent memory leaks.
   */
  private fun scheduleCleanup() {
    cleanupScheduled = true
    handler.postDelayed({
      synchronized(buffer) {
        val currentTime = System.currentTimeMillis()
        val keysToRemove = buffer.entries
          .filter { currentTime - it.value.firstReceivedTime > TIMEOUT_MS * 2 }
          .map { it.key }
        
        keysToRemove.forEach { buffer.remove(it) }
        
        // Schedule next cleanup if buffer is not empty
        cleanupScheduled = false
        if (buffer.isNotEmpty()) {
          scheduleCleanup()
        }
      }
    }, CLEANUP_INTERVAL_MS)
  }

  /**
   * Clears all buffered messages. Useful for testing or manual cleanup.
   */
  fun clear() {
    synchronized(buffer) {
      buffer.clear()
      handler.removeCallbacksAndMessages(null)
      cleanupScheduled = false
    }
  }

  /**
   * Gets the current number of message groups being buffered.
   */
  fun getBufferSize(): Int = buffer.size
}
