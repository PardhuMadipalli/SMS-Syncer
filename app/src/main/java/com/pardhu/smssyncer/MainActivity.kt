// MainActivity.kt
package com.pardhu.smssyncer

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import java.util.concurrent.Executors
import androidx.core.graphics.toColorInt
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : Activity() {

    companion object {
        private const val SMS_PERMISSION_REQUEST = 1
    }

    private var smsReceiver: SmsReceiver? = null
    private val executor = Executors.newSingleThreadExecutor()

    // UI elements
    private lateinit var statusText: TextView
    private lateinit var topicText: TextView
    private lateinit var requestPermissionButton: MaterialButton
    private lateinit var configureTopicButton: MaterialButton
    private lateinit var navTopicItem: LinearLayout
    private lateinit var navFiltersItem: LinearLayout
    private lateinit var navLogsItem: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        statusText = findViewById(R.id.statusText)
        topicText = findViewById(R.id.topicText)
        requestPermissionButton = findViewById(R.id.requestPermissionButton)
        configureTopicButton = findViewById(R.id.configureTopicButton)
        navTopicItem = findViewById(R.id.navTopicItem)
        navFiltersItem = findViewById(R.id.navFiltersItem)
        navLogsItem = findViewById(R.id.navLogsItem)

        // Set up button click listeners
        requestPermissionButton.setOnClickListener { requestAllPermissions() }
        configureTopicButton.setOnClickListener { showTopicConfigDialog() }
        
        // Set up bottom navigation
        setupBottomNavigation()
        
        // Check topic configuration first
        checkTopicConfiguration()
        

    }


    private fun checkTopicConfiguration() {
        if (TopicManager.isTopicConfigured(this)) {
            // Topic is configured, check password
            updateTopicDisplay()
            checkPasswordConfiguration()
        } else {
            // Topic not configured, show configuration dialog
            updateStatus("Topic Configuration Required", R.color.warning)
            topicText.text = "Not configured"
            configureTopicButton.visibility = View.VISIBLE
            showTopicConfigDialog()
        }
    }
    
    private fun checkPasswordConfiguration() {
        if (PasswordManager.isPasswordConfigured(this)) {
            // Password is configured, check permissions
            checkAndRequestAllPermissions()
        } else {
            // Password not configured, show configuration dialog
            updateStatus("Password Configuration Required", R.color.warning)
            showPasswordConfigDialog()
        }
    }

    private fun checkAndRequestAllPermissions() {
        try {
            if (PermissionManager.areAllPermissionsGranted(this)) {
                // All permissions granted
                updateStatus("Active - Monitoring SMS", R.color.success)
                // SMS receiver is automatically registered via AndroidManifest.xml
            } else {
                // Permissions not granted - request them immediately
                updateStatus("Permissions Required", R.color.error)
                requestPermissionButton.visibility = View.VISIBLE
                requestAllPermissions()
            }
        } catch (e: Exception) {
            // Handle any permission-related crashes
            updateStatus("Permission Error - Please restart app", R.color.error)
            requestPermissionButton.visibility = View.VISIBLE
        }
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = PermissionManager.getPermissionsToRequest(this)

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest, SMS_PERMISSION_REQUEST)
        } else {
            // All permissions already granted
            updateStatus("Active - Monitoring SMS", R.color.success)
            setupSmsReceiverWithNotification()
        }
    }

    private fun updateStatus(status: String, colorRes: Int) {
        statusText.text = status
        statusText.setTextColor(getColor(colorRes))
    }

    private fun setupSmsReceiver() {
        smsReceiver = SmsReceiver()
        val filter = IntentFilter().apply {
            addAction("android.provider.Telephony.SMS_RECEIVED")
            priority = 1000
        }
        registerReceiver(smsReceiver, filter)
    }

    private fun setupSmsReceiverWithNotification() {
        setupSmsReceiver()
        Toast.makeText(this, "SMS Syncer is running and monitoring messages", Toast.LENGTH_SHORT)
                .show()
    }



    override fun onDestroy() {
        super.onDestroy()
        smsReceiver?.let { unregisterReceiver(it) }
        executor.shutdown()
    }

    private fun showTopicConfigDialog() {
        TopicConfigDialog(this).show()
    }
    
    private fun showPasswordConfigDialog() {
        PasswordConfigDialog(this).show()
    }

    private fun setupBottomNavigation() {
        navTopicItem.setOnClickListener {
            showTopicConfigDialog()
        }
        
        navFiltersItem.setOnClickListener {
            val intent = android.content.Intent(this, FilterSettingsActivity::class.java)
            startActivity(intent)
        }
        
        navLogsItem.setOnClickListener {
            val intent = android.content.Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }
    }
    


    private fun updateTopicDisplay() {
        val topic = TopicManager.getTopic(this)
        if (topic != null) {
            val maskedTopic = TopicManager.getMaskedTopic(topic)
            topicText.text = maskedTopic
        } else {
            topicText.text = "Not configured"
        }
    }

    fun onTopicConfigured() {
        updateTopicDisplay()
        configureTopicButton.visibility = View.GONE
        checkPasswordConfiguration()
    }
    
    fun onPasswordConfigured() {
        checkAndRequestAllPermissions()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                // All permissions granted
                updateStatus("Active - Monitoring SMS", R.color.success)
                requestPermissionButton.visibility = View.GONE
                setupSmsReceiverWithNotification()
            } else {
                // Some permissions denied
                updateStatus("Permission Denied", R.color.error)
                requestPermissionButton.visibility = View.VISIBLE

                // Show explanation and provide option to open settings
                Toast.makeText(
                                this,
                                "All permissions are required for the app to work properly. Please grant permissions in Settings.",
                                Toast.LENGTH_LONG
                        )
                        .show()

                // Update button to open settings
                requestPermissionButton.text = "Open Settings"
                requestPermissionButton.setOnClickListener {
                    val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", packageName, null)
                            }
                    startActivity(intent)
                }
            }
        }
    }
}
