package com.unitx.hyphen_android.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Responsible solely for sharing or opening the log file via Android Intents.
 */
internal class LogShareHelper(
    private val fileProviderAuthority: String,
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
) {

    fun share(activityContext: Context, logFile: File, chooserTitle: String = "Share Logs") {
        val sharedFile = stageCopy(activityContext, logFile) ?: return
        val uri = getUri(activityContext, sharedFile) ?: return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "App Logs - ${dateFormat.format(Date())}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activityContext.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun open(activityContext: Context, logFile: File) {
        val sharedFile = stageCopy(activityContext, logFile) ?: return
        val uri = getUri(activityContext, sharedFile) ?: return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activityContext.startActivity(Intent.createChooser(intent, "Open Logs With"))
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun stageCopy(activityContext: Context, logFile: File): File? {
        if (!logFile.exists() || logFile.length() == 0L) {
            Log.w("Hyphen-Logger", "LogShareHelper: no logs to share — file missing or empty: ${logFile.absolutePath}")
            Toast.makeText(activityContext, "No logs available.", Toast.LENGTH_SHORT).show()
            return null
        }
        return try {
            val dest = File(activityContext.cacheDir, "app_logs.txt")
            copy(logFile, dest)
            dest
        } catch (e: Exception) {
            Log.e("Hyphen-Logger", "LogShareHelper: failed to stage log copy to cache", e)
            Toast.makeText(activityContext, "Failed to prepare logs: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun getUri(activityContext: Context, file: File): Uri? {
        return try {
            FileProvider.getUriForFile(activityContext, fileProviderAuthority, file)
        } catch (e: Exception) {
            Log.e("Hyphen-Logger", "LogShareHelper: FileProvider URI failed — authority=$fileProviderAuthority, file=${file.absolutePath}. Check that the <provider> is declared in AndroidManifest.xml with the correct authority and file_provider_paths.", e)
            Toast.makeText(activityContext, "Failed to get file URI: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun copy(src: File, dst: File) {
        FileInputStream(src).use { input ->
            FileOutputStream(dst).use { output ->
                val buffer = ByteArray(1024)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    output.write(buffer, 0, len)
                }
            }
        }
    }
}