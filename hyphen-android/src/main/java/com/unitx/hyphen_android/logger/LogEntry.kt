package com.unitx.hyphen_android.logger

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
    val timestamp: Date = Date()
) {
    companion object {
        // Shared, not per-instance — SimpleDateFormat is not thread-safe,
        // but LogFileManager.write() is @Synchronized so this is safe.
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    }

    fun format(): String {
        val ts = dateFormat.format(timestamp)
        val stackTrace = throwable?.let { t ->
            buildString {
                append("\n")
                append(t.toString())
                append("\n")
                t.stackTrace.forEach { append("\tat $it\n") }
            }
        } ?: ""
        return "[$ts] [${level.tag}] [$tag]: $message$stackTrace"
    }
}