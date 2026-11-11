package com.amaterasu.expense_tracker.worker

import android.content.Context
import com.amaterasu.expense_tracker.logging.AppLogger
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    const val backgroundSmsWorkerEnabled = false
    private const val WORK_NAME = "sms_import_worker"

    fun schedule(context: Context) {
        if (!backgroundSmsWorkerEnabled) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
            workManager.cancelAllWorkByTag("USER_TRIGGERED")
            AppLogger.d("WORK_SCHEDULER", "Background SMS worker disabled; stale SMS work cancelled")
            return
        }

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
        if (!backgroundSmsWorkerEnabled) {
            AppLogger.d("WORK_SCHEDULER", "Background SMS worker disabled by flag")
            return
        }

        AppLogger.d("WORKER_RUN_ONCE", "User triggered run")

        val request = OneTimeWorkRequestBuilder<SmsImportWorker>()
            .addTag("USER_TRIGGERED")   // 👈 key
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
