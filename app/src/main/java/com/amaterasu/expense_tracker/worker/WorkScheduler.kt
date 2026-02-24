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
    private const val WORK_NAME = "sms_import_worker";

    fun schedule(context: Context) {
        val constraints  = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build();

        val request = PeriodicWorkRequestBuilder<SmsImportWorker>(30, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build();

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    // 🔧 Debug only (manual trigger)
    fun runOnce(context: Context) {
        Log.d("WORKER_RUN_ONCE", "WORKER_RUN_ONCE");

        WorkManager.getInstance(context)
            .enqueue(
                OneTimeWorkRequestBuilder<SmsImportWorker>().build()
            )
    }

}