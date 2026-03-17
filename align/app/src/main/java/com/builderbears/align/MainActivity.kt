package com.builderbears.align

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.builderbears.align.data.service.NotificationService
import com.builderbears.align.ui.navigation.AlignApp
import com.builderbears.align.ui.theme.AlignTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationService.createChannels(this)
        enableEdgeToEdge()
        setContent {
            AlignTheme { AlignApp() }
        }
    }
}
