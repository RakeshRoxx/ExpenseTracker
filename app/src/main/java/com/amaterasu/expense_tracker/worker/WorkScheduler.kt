package com.amaterasu.expense_tracker.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val WORK_NAME = "sms_import_worker"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<SmsImportWorker>(
            15, TimeUnit.MINUTES  // ✅ minimum allowed
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    // ✅ User-triggered run → allow Foreground Service
    fun runOnceFromUser(context: Context) {
        Log.d("WORKER_RUN_ONCE", "User triggered run")

        val request = OneTimeWorkRequestBuilder<SmsImportWorker>()
            .addTag("USER_TRIGGERED")   // 👈 key
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}