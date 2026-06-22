package com.unitx.hyphen_android.logger

import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Responsible solely for reading the system Logcat stream
 * and forwarding matching lines to a consumer.
 */
internal class LogcatCapture(
    private val onLine: (String) -> Unit
) {

    private var thread: Thread? = null
    private var isCapturing = false

    fun start() {
        if (isCapturing) return
        isCapturing = true

        thread = Thread {
            val pid = Process.myPid().toString()
            var process: java.lang.Process? = null
            var reader: BufferedReader? = null
            try {
                process = Runtime.getRuntime().exec("logcat -v threadtime")
                reader = BufferedReader(InputStreamReader(process.inputStream))

                var line: String?
                while (isCapturing) {
                    line = reader.readLine() ?: break
                    if (line.contains(pid)) {
                        onLine(line)
                    }
                }
            } catch (e: Exception) {
                Log.e("Hyphen-Logger", "LogcatCapture: failed to read logcat stream", e)
            } finally {
                runCatching { reader?.close() }
                process?.destroy()
            }
        }.also {
            it.isDaemon = true
            it.start()
        }
    }

    fun stop() {
        isCapturing = false
        thread?.interrupt()
        thread = null
    }

    val isRunning: Boolean get() = isCapturing
}