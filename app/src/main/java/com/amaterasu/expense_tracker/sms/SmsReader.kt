package com.amaterasu.expense_tracker.sms

import android.content.Context
import android.provider.Telephony
import com.amaterasu.expense_tracker.logging.AppLogger
import com.amaterasu.expense_tracker.ml.LiteRtTransactionClassifier
import utils.Utils

class SmsReader(private val context: Context) {

    private val classifier by lazy { LiteRtTransactionClassifier(context) }

    suspend fun readInbox(): List<RawSms> {
        AppLogger.d("SMS_FLOW", "Reading SMS inbox");

        val lastTs = SmsSyncStore.getLastTimestamp(context);

        AppLogger.d("SMS_FLOW", "Last Ts ${Utils.formatTimeStamp(lastTs)}");

        val result = mutableListOf<RawSms>()

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS
            ),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(lastTs.toString()),
            "${Telephony.Sms.DATE} ASC"
        )

        cursor?.use {
            AppLogger.d("SMS_FLOW", "SMS cursor count = ${it.count}")

            while (it.moveToNext()) {
                val body = it.getString(0) ?: ""
                val date = it.getLong(1)
                val sender = it.getString(2) ?: ""

                val classification = classifier.classify(body, sender)
                val excludedSender = sender.contains("EPFO", ignoreCase = true)
                val shouldImport = !excludedSender && classification.isFinancialTransaction

                AppLogger.d(
                    "SMS_ML_CLASSIFICATION",
                    "time=${Utils.formatTimeStamp(date)}, sender=$sender, " +
                        "purpose=${classification.purpose}, direction=${classification.direction}, " +
                        "confidence=${classification.confidence}, financial=${classification.isFinancialTransaction}, " +
                        "excludedSender=$excludedSender, imported=$shouldImport, " +
                        "explanation=${classification.explanation}"
                )

                if (shouldImport) {
                    AppLogger.d("looksLikeTransaction", body)
                    result.add(RawSms(body, date, sender))
                }
            }
        }
        AppLogger.d("SMS_FLOW", "Filtered ${result.size} transaction SMS")

        if (result.isNotEmpty()) {
            SmsSyncStore.saveLastTimestamp(context, result.last().timestamp);
        }

        return result
    }

}
