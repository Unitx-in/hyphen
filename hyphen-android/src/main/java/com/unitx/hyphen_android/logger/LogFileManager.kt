package com.unitx.hyphen_android.logger

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

/**
 * Responsible solely for reading, writing, rotating, and clearing the log file.
 */
internal class LogFileManager(context: Context) {

    companion object {
        private const val LOG_FILE_NAME = "app_logs.txt"
        private const val BACKUP_FILE_NAME = "app_logs_backup.txt"
        private const val MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024 // 5 MB
    }

    val logFile: File = File(context.filesDir, LOG_FILE_NAME)
    private val backupFile: File = File(context.filesDir, BACKUP_FILE_NAME)

    @Synchronized
    fun write(line: String) {
        rotateIfNeeded()
        try {
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.write(line)
                writer.newLine()
            }
        } catch (e: Exception) {
            Log.e("Hyphen-Logger", "LogFileManager: failed to write log line: \"$line\"", e)
        }
    }

    private fun rotateIfNeeded() {
        if (logFile.exists() && logFile.length() >= MAX_FILE_SIZE_BYTES) {
            backupFile.delete()
            logFile.renameTo(backupFile)
        }
    }

    @Synchronized
    fun clear() {
        try {
            FileWriter(logFile, false).use { it.write("") }
        } catch (e: Exception) {
            Log.e("Hyphen-Logger", "LogFileManager: failed to clear log file", e)
        }
    }

    fun read(): String {
        if (!logFile.exists()) return ""
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(Files.readAllBytes(logFile.toPath()))
            } else {
                logFile.readText()
            }
        } catch (e: Exception) {
            Log.e("Hyphen-Logger", "LogFileManager: failed to read log file", e)
            ""
        }
    }

    fun exists(): Boolean = logFile.exists() && logFile.length() > 0
}