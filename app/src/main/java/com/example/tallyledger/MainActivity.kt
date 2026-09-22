package com.example.tallyledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallyledger.ui.screens.LedgerDashboardScreen
import com.example.tallyledger.ui.theme.TallyLedgerTheme
import com.example.tallyledger.ui.viewmodel.LedgerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TallyLedgerTheme {
                val viewModel: LedgerViewModel = viewModel()
                LedgerDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
