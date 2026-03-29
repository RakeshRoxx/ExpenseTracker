package com.amaterasu.expense_tracker.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.amaterasu.expense_tracker.R
import com.amaterasu.expense_tracker.data.database.RoomDatabaseProvider
import com.amaterasu.expense_tracker.data.repository.TransactionRepository
import com.amaterasu.expense_tracker.notification.NotificationHelper
import com.amaterasu.expense_tracker.sms.SmsPermission
import com.amaterasu.expense_tracker.usecase.ImportSmsTransactionsUseCase

class SmsImportWorker (
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        Log.d("SMS_WORKER", "Worker Started")

        if (!SmsPermission.hasPermission(applicationContext)) {
            Log.d("SMS_WORKER", "SMS permission not granted can't process")
            return Result.success()
        }

        val isUserTriggered = tags.contains("USER_TRIGGERED")

        if (isUserTriggered) {
            // ✅ Foreground only for user action
            setForeground(createForegroundInfo("Importing transactions…"))
        }

        return try {
            val importUseCase = ImportSmsTransactionsUseCase(applicationContext)
            val db = RoomDatabaseProvider.get(applicationContext)
            val repository = TransactionRepository(db.TransactionDao())

            val transactions = importUseCase.execute()
            Log.d("SMS_WORKER", "Parsed ${transactions.size} transactions")

            repository.saveAll(transactions)
            Result.success()
        } catch (e: Exception) {
            Log.e("SMS_WORKER", "Worker failed", e)
            Result.retry()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(text: String) {

        if (!canPostNotifications()) {
            Log.d("SMS_WORKER", "Notification permission not granted, Skipping Notification");
            return;
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Expense Tracker")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();

        NotificationManagerCompat.from(applicationContext)
            .notify(1001, notification)
    }

    private fun dismissNotification() {
        if (!canPostNotifications()) return;

        NotificationManagerCompat.from(applicationContext).cancel(1001)
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // pre-Android 13
        }
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Expense Tracker")
            .setContentText(text)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= 29) {
            ForegroundInfo(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(1001, notification)
        }
    }
}