package com.pardhu.smssyncer

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial

/** Activity for customizing SMS filters */
class FilterSettingsActivity : AppCompatActivity() {

    private lateinit var importantSendersChipGroup: ChipGroup
    private lateinit var importantKeywordsChipGroup: ChipGroup
    private lateinit var spamKeywordsChipGroup: ChipGroup
    private lateinit var otpPatternInput: EditText
    private lateinit var forwardAllSwitch: SwitchMaterial
    private lateinit var resetButton: MaterialButton
    private lateinit var saveButton: MaterialButton

    // Temporary storage for editing
    private var tempImportantSenders = mutableListOf<String>()
    private var tempImportantKeywords = mutableListOf<String>()
    private var tempSpamKeywords = mutableListOf<String>()
    private var tempOtpPattern = ""
    private var tempForwardAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_settings)
        
        initializeViews()
        loadCurrentFilters()
        setupListeners()
        setupAddButtons()
    }

    private fun initializeViews() {
        importantSendersChipGroup = findViewById(R.id.importantSendersChipGroup)
        importantKeywordsChipGroup = findViewById(R.id.importantKeywordsChipGroup)
        spamKeywordsChipGroup = findViewById(R.id.spamKeywordsChipGroup)
        otpPatternInput = findViewById(R.id.otpPatternInput)
        forwardAllSwitch = findViewById(R.id.forwardAllSwitch)
        resetButton = findViewById(R.id.resetButton)
        saveButton = findViewById(R.id.saveButton)

        // Set up toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadCurrentFilters() {
        // Load current values
        tempImportantSenders = FilterManager.getImportantSenders(this).toMutableList()
        tempImportantKeywords = FilterManager.getImportantKeywords(this).toMutableList()
        tempSpamKeywords = FilterManager.getSpamKeywords(this).toMutableList()
        tempOtpPattern = FilterManager.getOtpPattern(this)
        tempForwardAll = FilterManager.getForwardAll(this)

        // Update UI
        updateChipGroup(importantSendersChipGroup, tempImportantSenders)
        updateChipGroup(importantKeywordsChipGroup, tempImportantKeywords)
        updateChipGroup(spamKeywordsChipGroup, tempSpamKeywords)
        otpPatternInput.setText(tempOtpPattern)
        forwardAllSwitch.isChecked = tempForwardAll
    }

    private fun updateChipGroup(chipGroup: ChipGroup, items: MutableList<String>) {
        chipGroup.removeAllViews()
        items.forEach { item ->
            val chip = createChip(item) { 
                items.remove(item)
                updateChipGroup(chipGroup, items) 
            }
            chipGroup.addView(chip)
        }
    }

    private fun createChip(text: String, onRemove: () -> Unit): Chip {
        return Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener { onRemove() }
            chipBackgroundColor = getColorStateList(R.color.primary_container)
            setTextColor(getColorStateList(R.color.on_primary_container))
        }
    }

    private fun setupListeners() {
        resetButton.setOnClickListener { showResetConfirmation() }
        saveButton.setOnClickListener { saveFilters() }
    }

    private fun setupAddButtons() {
        // Important Senders Add Button
        findViewById<MaterialButton>(R.id.addImportantSenderButton).setOnClickListener {
            showAddItemDialog("Add Important Sender") { sender ->
                if (sender.isNotEmpty() && !tempImportantSenders.contains(sender)) {
                    tempImportantSenders.add(sender)
                    updateChipGroup(importantSendersChipGroup, tempImportantSenders)
                }
            }
        }

        // Important Keywords Add Button
        findViewById<MaterialButton>(R.id.addImportantKeywordButton).setOnClickListener {
            showAddItemDialog("Add Important Keyword") { keyword ->
                if (keyword.isNotEmpty() && !tempImportantKeywords.contains(keyword)) {
                    tempImportantKeywords.add(keyword)
                    updateChipGroup(importantKeywordsChipGroup, tempImportantKeywords)
                }
            }
        }

        // Spam Keywords Add Button
        findViewById<MaterialButton>(R.id.addSpamKeywordButton).setOnClickListener {
            showAddItemDialog("Add Spam Keyword") { keyword ->
                if (keyword.isNotEmpty() && !tempSpamKeywords.contains(keyword)) {
                    tempSpamKeywords.add(keyword)
                    updateChipGroup(spamKeywordsChipGroup, tempSpamKeywords)
                }
            }
        }
    }

    private fun showAddItemDialog(title: String, onAdd: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Enter $title"
            setSingleLine()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    onAdd(text)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset to Defaults")
            .setMessage("This will reset all filters to their default values. Are you sure?")
            .setPositiveButton("Reset") { _, _ -> resetToDefaults() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetToDefaults() {
        if (FilterManager.resetToDefaults(this)) {
            loadCurrentFilters()
            Toast.makeText(this, "Filters reset to defaults", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to reset filters", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFilters() {
        // Update temp values from UI
        tempOtpPattern = otpPatternInput.text.toString().trim()
        tempForwardAll = forwardAllSwitch.isChecked

        // Validate OTP pattern
        if (tempOtpPattern.isNotEmpty()) {
            try {
                java.util.regex.Pattern.compile(tempOtpPattern)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid OTP pattern. Please enter a valid regex.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Save all filters
        val success = FilterManager.saveImportantSenders(this, tempImportantSenders) &&
                FilterManager.saveImportantKeywords(this, tempImportantKeywords) &&
                FilterManager.saveSpamKeywords(this, tempSpamKeywords) &&
                FilterManager.saveOtpPattern(this, tempOtpPattern) &&
                FilterManager.saveForwardAll(this, tempForwardAll)

        if (success) {
            Toast.makeText(this, "Filters saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save filters", Toast.LENGTH_SHORT).show()
        }
    }
}
