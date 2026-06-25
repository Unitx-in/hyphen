package com.unitx.hyphen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.unitx.hyphen.ui.theme.HyphenTheme
import com.unitx.hyphen_kotlin.ResultUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startLogcatCapture(this)

        enableEdgeToEdge()
        setContent {
            HyphenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        LoggerTestScreen()
                        ResultUnit.SuccessUnit("Success")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLogcatCapture()
    }
}

