package com.pardhu.smssyncer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Activity for viewing error logs */
class LogsActivity : AppCompatActivity() {

    private lateinit var logsRecyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var clearButton: MaterialButton
    private lateinit var logsAdapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        
        initializeViews()
        loadLogs()
    }

    private fun initializeViews() {
        logsRecyclerView = findViewById(R.id.logsRecyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)
        clearButton = findViewById(R.id.clearLogsButton)
        
        // Set up toolbar
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            finish()
        }
        
        // Set up RecyclerView
        logsRecyclerView.layoutManager = LinearLayoutManager(this)
        logsAdapter = LogsAdapter()
        logsRecyclerView.adapter = logsAdapter
        
        // Set up clear button
        clearButton.setOnClickListener { showClearConfirmation() }
    }

    private fun loadLogs() {
        val logs = LogManager.getLogs(this)
        logsAdapter.setLogs(logs)
        
        // Show/hide empty state
        if (logs.isEmpty()) {
            logsRecyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
            clearButton.isEnabled = false
        } else {
            logsRecyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            clearButton.isEnabled = true
        }
    }

    private fun showClearConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear All Logs")
            .setMessage("This will delete all error logs. Are you sure?")
            .setPositiveButton("Clear") { _, _ -> clearLogs() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearLogs() {
        LogManager.clearLogs(this)
        loadLogs()
        Toast.makeText(this, "All logs cleared", Toast.LENGTH_SHORT).show()
    }

    /** Adapter for displaying logs */
    private class LogsAdapter : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {
        
        private var logs = listOf<LogManager.LogEntry>()
        
        fun setLogs(newLogs: List<LogManager.LogEntry>) {
            logs = newLogs
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log_entry, parent, false)
            return LogViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.bind(logs[position])
        }
        
        override fun getItemCount() = logs.size
        
        class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val timestampText: TextView = itemView.findViewById(R.id.logTimestamp)
            private val levelText: TextView = itemView.findViewById(R.id.logLevel)
            private val messageText: TextView = itemView.findViewById(R.id.logMessage)
            private val detailsText: TextView = itemView.findViewById(R.id.logDetails)
            
            fun bind(log: LogManager.LogEntry) {
                timestampText.text = LogManager.formatTimestamp(log.timestamp)
                levelText.text = log.level
                messageText.text = log.message
                
                // Set level color
                val context = itemView.context
                val levelColor = when (log.level) {
                    LogManager.LEVEL_ERROR -> context.getColor(R.color.error)
                    LogManager.LEVEL_WARNING -> context.getColor(R.color.warning)
                    else -> context.getColor(R.color.success)
                }
                levelText.setTextColor(levelColor)
                
                // Show/hide details
                if (log.details.isNullOrEmpty()) {
                    detailsText.visibility = View.GONE
                } else {
                    detailsText.visibility = View.VISIBLE
                    detailsText.text = log.details
                }
            }
        }
    }
}
