package com.amaterasu.expense_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.amaterasu.expense_tracker.ui.screens.DashboardScreen
import com.amaterasu.expense_tracker.ui.theme.ExpensetrackerTheme
import com.amaterasu.expense_tracker.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cancel any stale background SMS jobs. SMS imports now happen only after user consent.
        WorkScheduler.schedule(this)

        setContent {
            ExpensetrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    DashboardScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
