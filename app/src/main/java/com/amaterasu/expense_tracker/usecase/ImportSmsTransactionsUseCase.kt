package com.amaterasu.expense_tracker.usecase

import android.content.Context
import domain.Transaction
import com.amaterasu.expense_tracker.sms.RawSms
import com.amaterasu.expense_tracker.sms.SmsProcessor
import com.amaterasu.expense_tracker.sms.SmsReader

class ImportSmsTransactionsUseCase(
    context: Context,
    private val llmFallback: (suspend (RawSms) -> Transaction?)? = null
) {
    private val reader = SmsReader(context)
    private val processor = SmsProcessor()

    suspend fun execute(): List<Transaction> {
        val rawSms = reader.readInbox()
        val results = mutableListOf<Transaction>()

        for (sms in rawSms) {
            var txn = processor.parseOne(sms)

            if (txn == null && llmFallback != null) {
                txn = llmFallback.invoke(sms)
            }

            if (txn != null) results.add(txn)
        }

        return results
    }
}