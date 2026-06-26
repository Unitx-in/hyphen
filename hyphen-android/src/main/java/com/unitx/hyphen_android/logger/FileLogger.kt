package com.unitx.hyphen_android.logger

import android.content.Context
import android.util.Log

/**
 * FileLogger — public singleton facade.
 *
 * Responsibilities delegated to:
 *  - [LogFileManager]   → file I/O, rotation, read, clear
 *  - [LogcatCapture]    → logcat stream capture
 *  - [LogShareHelper]   → share / open intents
 *
 * Usage:
 * ```
 * // In Application.onCreate()
 * FileLogger.init(this)
 * FileLogger.startLogcatCapture()
 *
 * // Anywhere in the app
 * FileLogger.d("MyTag", "Something happened")
 * FileLogger.e("MyTag", "Oops", exception)
 *
 * // From an Activity
 * FileLogger.share(this)
 * FileLogger.open(this)
 * FileLogger.clear()
 * ```
 */
object FileLogger {

    private lateinit var fileManager: LogFileManager
    private lateinit var shareHelper: LogShareHelper
    private lateinit var logcatCapture: LogcatCapture

    private var initialized = false

    // ─── Init ─────────────────────────────────────────────────────────────────

    /**
     * Must be called once before any other method — typically in [Application.onCreate].
     */
    fun init(context: Context, fileProviderAuthority: String = context.applicationContext.packageName + ".provider") {
        if (initialized) {
            Log.w("Hyphen-Logger", "FileLogger.init() called more than once — ignoring.")
            return
        }
        val appContext = context.applicationContext
        fileManager = LogFileManager(appContext)
        shareHelper = LogShareHelper(fileProviderAuthority)
        logcatCapture = LogcatCapture(onLine = { fileManager.write(it) })
        initialized = true
        Log.i("Hyphen-Logger", "FileLogger initialized. Log file: ${fileManager.logFile.absolutePath}")
    }

    // ─── Logcat capture ───────────────────────────────────────────────────────

    /** Starts capturing all Logcat output, filtered to this app's PID. */
    fun startLogcatCapture() {
        checkInit()
        logcatCapture.start()
    }

    /** Stops logcat capture. */
    fun stopLogcatCapture() {
        checkInit()
        logcatCapture.stop()
    }

    val isCapturing: Boolean get() = if (initialized) logcatCapture.isRunning else false

    // ─── Manual log methods ───────────────────────────────────────────────────

    fun d(tag: String, message: String) = write(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = write(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = write(LogLevel.WARNING, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        write(LogLevel.ERROR, tag, message, throwable)

    private fun write(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        checkInit()
        val entry = LogEntry(level, tag, message, throwable)
        fileManager.write(entry.format())
    }

    // ─── File access ─────────────────────────────────────────────────────────

    fun readLogs(): String {
        checkInit()
        return fileManager.read()
    }

    fun clear() {
        checkInit()
        fileManager.clear()
    }

    // ─── Share / Open ─────────────────────────────────────────────────────────

    fun share(activityContext: Context, chooserTitle: String = "Share Logs") {
        checkInit()
        shareHelper.share(activityContext, fileManager.logFile, chooserTitle)
    }

    fun open(activityContext: Context) {
        checkInit()
        shareHelper.open(activityContext, fileManager.logFile)
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private fun checkInit() {
        if (!initialized) {
            val msg = "FileLogger is not initialized. Call FileLogger.init(context) first."
            Log.e("Hyphen-Logger", msg)
            error(msg)
        }
    }
}