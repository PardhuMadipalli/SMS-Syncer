package com.pardhu.smssyncer

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/** Dialog for configuring the encryption password */
class PasswordConfigDialog(private val context: Context) {
    
    /**
     * Shows the password configuration dialog
     */
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_password_config, null)
        val titleText = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmButton = dialogView.findViewById<MaterialButton>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        
        titleText.text = "Set Encryption Password"
        messageText.text = "Enter a password to encrypt SMS messages. This same password must be configured in your shell script for decryption."
        
        // Set password input type
        passwordInput.transformationMethod = PasswordTransformationMethod()
        
        // Clear any previous input
        passwordInput.text?.clear()
        
        // Add text change listener to enable/disable confirm button
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                confirmButton.isEnabled = password.length >= 8
            }
        })
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        confirmButton.setOnClickListener {
            val password = passwordInput.text?.toString() ?: ""
            if (password.length >= 8) {
                // Save the password
                if (PasswordManager.savePassword(context, password)) {
                    Toast.makeText(context, "Password configured successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    // Notify the activity that password is configured
                    (context as? MainActivity)?.onPasswordConfigured()
                } else {
                    Toast.makeText(context, "Failed to save password. Please try again.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(context, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            }
        }
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        // Initially disable confirm button
        confirmButton.isEnabled = false
        
        dialog.show()
    }
}
