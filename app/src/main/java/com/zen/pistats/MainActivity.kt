package com.zen.pistats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zen.pistats.app.PiStatsApp
import com.zen.pistats.ui.theme.PiStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiStatsTheme {
                PiStatsApp()
            }
        }
    }
}
