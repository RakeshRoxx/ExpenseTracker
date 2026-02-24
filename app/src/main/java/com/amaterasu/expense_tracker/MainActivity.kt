package com.amaterasu.expense_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.notification.NotificationHelper
import com.amaterasu.expense_tracker.sms.SmsPermission
import com.amaterasu.expense_tracker.ui.screens.DashboardScreen
import com.amaterasu.expense_tracker.ui.theme.ExpensetrackerTheme
import com.amaterasu.expense_tracker.viewmodel.MainViewModel
import com.amaterasu.expense_tracker.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                WorkScheduler.schedule(this)
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        NotificationHelper.createChannel(this)

        setContent {
            ExpensetrackerTheme {

                val mainViewModel: MainViewModel = viewModel()

                LaunchedEffect(Unit) {
                    // Request SMS permission if needed
                    if (!SmsPermission.hasPermission(this@MainActivity)) {
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    } else {
                        WorkScheduler.schedule(this@MainActivity)
                    }

                    // Request notification permission on Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    DashboardScreen(
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
