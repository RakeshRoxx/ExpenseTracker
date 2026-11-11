package com.amaterasu.expense_tracker.worker

import android.content.Context
import com.amaterasu.expense_tracker.logging.AppLogger
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amaterasu.expense_tracker.data.database.RoomDatabaseProvider
import com.amaterasu.expense_tracker.data.repository.TransactionRepository
import com.amaterasu.expense_tracker.sms.SmsPermission
import com.amaterasu.expense_tracker.usecase.ImportSmsTransactionsUseCase

class SmsImportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppLogger.d("SMS_WORKER", "Worker Started")

        if (!SmsPermission.hasPermission(applicationContext)) {
            AppLogger.d("SMS_WORKER", "SMS permission not granted; cannot process")
            return Result.success()
        }

        return try {
            val importUseCase = ImportSmsTransactionsUseCase(applicationContext)
            val db = RoomDatabaseProvider.get(applicationContext)
            val repository = TransactionRepository(db.TransactionDao())

            val transactions = importUseCase.execute()
            AppLogger.d("SMS_WORKER", "Parsed ${transactions.size} transactions")

            repository.saveAll(transactions)
            Result.success()
        } catch (e: Exception) {
            AppLogger.e("SMS_WORKER", "Worker failed", e)
            Result.retry()
        }
    }
}
