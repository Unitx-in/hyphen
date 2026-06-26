package com.unitx.hyphen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unitx.hyphen_android.logger.FileLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty


private const val TAG = "LoggerTest"

fun startLogcatCapture(context: Context){
    FileLogger.init(context, "${context.packageName}.fileprovider")
    FileLogger.startLogcatCapture()
    FileLogger.i(TAG, "MainActivity created — logger ready")
}

fun stopLogcatCapture(){
    FileLogger.stopLogcatCapture()
}
@Composable
fun LoggerTestScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Live log lines shown in the UI
    val logLines = remember { mutableStateListOf<Pair<String, Color>>() }
    val listState = rememberLazyListState()

    fun appendLine(text: String, color: Color = Color(0xFFE0E0E0)) {
        logLines.add(text to color)
        scope.launch { listState.animateScrollToItem(logLines.lastIndex) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "FileLogger",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "hyphen-android · test harness",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        HorizontalDivider()

        // ── Action buttons ────────────────────────────────────────────────────
        val buttonRows: List<List<Pair<String, () -> Unit>>> = listOf(
            listOf(
                "Log DEBUG" to {
                    FileLogger.d(TAG, "Debug message fired")
                    appendLine("Debug message fired", Color(0xFF90CAF9))
                },
                "Log INFO" to {
                    FileLogger.i(TAG, "Info message fired")
                    appendLine("[I] Info message fired", Color(0xFFA5D6A7))
                }
            ),
            listOf(
                "Log WARNING" to {
                    FileLogger.w(TAG, "Warning message fired")
                    appendLine("[W] Warning message fired", Color(0xFFFFCC80))
                },
                "Log ERROR" to {
                    FileLogger.e(TAG, "Error message fired", RuntimeException("test error"))
                    appendLine("[E] Error with exception", Color(0xFFEF9A9A))
                }
            ),
            listOf(
                "Stress (50 lines)" to {
                    scope.launch {
                        repeat(50) { i ->
                            FileLogger.d(TAG, "Stress line #$i")
                            appendLine("[D] Stress line #$i", Color(0xFFCE93D8))
                            delay(30)
                        }
                    }
                },
                "Share log" to {
                    FileLogger.share(context)
                }
            ),
            listOf(
                "Open log" to {
                    FileLogger.open(context)
                },
                "Read → UI" to {
                    val content = FileLogger.readLogs()
                    val preview = content.takeLast(300).ifEmpty { "(empty)" }
                    appendLine("── readLogs() last 300 chars ──\n$preview", Color(0xFFFFF176))
                }
            ),
            listOf(
                "Clear logs" to {
                    FileLogger.clear()
                    logLines.clear()
                    appendLine("Logs cleared.", Color(0xFFB0BEC5))
                }
            )
        )

        buttonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, action) ->
                    Button(
                        onClick = action,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(label, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Live output panel ─────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Output",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${logLines.size} lines",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            if (logLines.isEmpty()) {
                Text(
                    text = "Tap a button to write logs…",
                    color = Color(0xFF555577),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(state = listState) {
                    items(logLines) { (line, color) ->
                        Text(
                            text = line,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}