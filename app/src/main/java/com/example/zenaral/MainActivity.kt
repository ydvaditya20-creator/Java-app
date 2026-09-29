package com.example.zenaral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zenaral.ui.screens.LedgerDashboardScreen
import com.example.zenaral.ui.theme.ZenaralTheme
import com.example.zenaral.ui.viewmodel.LedgerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LedgerViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
            ZenaralTheme(themeMode = currentTheme) {
                LedgerDashboardScreen(viewModel = viewModel)
            }
        }
    }
}

