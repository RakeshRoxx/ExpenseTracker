package com.amaterasu.expense_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.amaterasu.expense_tracker.notification.NotificationHelper
import com.amaterasu.expense_tracker.sms.SmsPermission
import com.amaterasu.expense_tracker.ui.screens.DashboardScreen
import com.amaterasu.expense_tracker.ui.theme.ExpensetrackerTheme
import com.amaterasu.expense_tracker.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (WorkScheduler.backgroundSmsWorkerEnabled) {
                    maybeRequestNotificationPermission()
                } else {
                    Log.d("PERMISSION", "READ_SMS granted")
                }
            } else {
                Log.w("PERMISSION", "READ_SMS denied")
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            maybeRequestFgsPermission()
        }

    private val fgsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                WorkScheduler.schedule(this)
            } else {
                Log.w("PERMISSION", "FOREGROUND_SERVICE_DATA_SYNC denied")
                // You can still run background work without FGS, just no foreground service
                WorkScheduler.schedule(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (WorkScheduler.backgroundSmsWorkerEnabled) {
            NotificationHelper.createChannel(this)
        }

        setContent {
            ExpensetrackerTheme {

                LaunchedEffect(Unit) {
                    requestPermissionsInOrder()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    DashboardScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }

    private fun requestPermissionsInOrder() {
        if (!SmsPermission.hasPermission(this)) {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            return
        }

        if (WorkScheduler.backgroundSmsWorkerEnabled) {
            maybeRequestNotificationPermission()
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        maybeRequestFgsPermission()
    }

    private fun maybeRequestFgsPermission() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                fgsPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
                return
            }
        }

        // ✅ All required permissions handled → schedule work
        WorkScheduler.schedule(this)
    }
}
