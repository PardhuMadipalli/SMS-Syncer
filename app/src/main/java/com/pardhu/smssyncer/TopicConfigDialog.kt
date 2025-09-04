package com.pardhu.smssyncer

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/** Dialog for configuring the ntfy topic name with confirmation */
class TopicConfigDialog(private val context: Context) {
    
    private var currentStep = 1
    private var firstEntry = ""
    private var secondEntry = ""
    
    /**
     * Shows the topic configuration dialog
     */
    fun show() {
        if (currentStep == 1) {
            showFirstEntryDialog()
        }
    }
    
    private fun showFirstEntryDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_topic_config, null)
        val titleText = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val topicInput = dialogView.findViewById<TextInputEditText>(R.id.topicInput)
        val confirmButton = dialogView.findViewById<MaterialButton>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        
        titleText.text = "Configure ntfy Topic"
        messageText.text = "Enter your ntfy.sh topic name. This will be stored securely on your device."
        
        // Clear any previous input
        topicInput.text?.clear()
        
        // Add text change listener to enable/disable confirm button
        topicInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                confirmButton.isEnabled = s?.isNotEmpty() ?: false
            }
        })
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        confirmButton.setOnClickListener {
            val topic = topicInput.text?.toString()?.trim() ?: ""
            if (topic.isNotEmpty()) {
                firstEntry = topic
                currentStep = 2
                dialog.dismiss()
                showConfirmationDialog()
            } else {
                Toast.makeText(context, "Please enter a topic name", Toast.LENGTH_SHORT).show()
            }
        }
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        // Initially disable confirm button
        confirmButton.isEnabled = false
        
        dialog.show()
    }
    
    private fun showConfirmationDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_topic_config, null)
        val titleText = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val topicInput = dialogView.findViewById<TextInputEditText>(R.id.topicInput)
        val confirmButton = dialogView.findViewById<MaterialButton>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        
        titleText.text = "Confirm Topic"
        messageText.text = "Please enter the topic name again to confirm:"
        
        // Clear input
        topicInput.text?.clear()
        
        // Add text change listener
        topicInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                confirmButton.isEnabled = s?.isNotEmpty() ?: false
            }
        })
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        confirmButton.setOnClickListener {
            val topic = topicInput.text?.toString()?.trim() ?: ""
            if (topic.isNotEmpty()) {
                secondEntry = topic
                if (firstEntry == secondEntry) {
                    // Save the topic
                    if (TopicManager.saveTopic(context, topic)) {
                        Toast.makeText(context, "Topic configured successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        // Notify the activity that topic is configured
                        (context as? MainActivity)?.onTopicConfigured()
                    } else {
                        Toast.makeText(context, "Failed to save topic. Please try again.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "Topic names don't match. Please try again.", Toast.LENGTH_SHORT).show()
                    // Reset and start over
                    currentStep = 1
                    firstEntry = ""
                    secondEntry = ""
                    dialog.dismiss()
                    show()
                }
            } else {
                Toast.makeText(context, "Please enter the topic name", Toast.LENGTH_SHORT).show()
            }
        }
        
        cancelButton.setOnClickListener {
            // Reset and go back to first step
            currentStep = 1
            firstEntry = ""
            secondEntry = ""
            dialog.dismiss()
            show()
        }
        
        // Initially disable confirm button
        confirmButton.isEnabled = false
        
        dialog.show()
    }
}
