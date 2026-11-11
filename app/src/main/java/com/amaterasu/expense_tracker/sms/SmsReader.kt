package com.amaterasu.expense_tracker.sms

import android.content.Context
import android.provider.Telephony
import com.amaterasu.expense_tracker.logging.AppLogger
import com.amaterasu.expense_tracker.ml.LiteRtTransactionClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.Utils

sealed interface SmsBodyLookupResult {
    data object Loading : SmsBodyLookupResult
    data class Found(val body: String) : SmsBodyLookupResult
    data object NotFound : SmsBodyLookupResult
    data object PermissionDenied : SmsBodyLookupResult
}

class SmsReader(private val context: Context) {

    private val classifier by lazy { LiteRtTransactionClassifier(context) }

    suspend fun readInbox(): List<RawSms> = withContext(Dispatchers.IO) {
        if (!SmsPermission.hasPermission(context)) {
            AppLogger.d("SMS_FLOW", "SMS permission not granted; skipping readInbox")
            return@withContext emptyList()
        }

        AppLogger.d("SMS_FLOW", "Reading SMS inbox")

        val lastTs = SmsSyncStore.getLastTimestamp(context)
        AppLogger.d("SMS_FLOW", "Last Ts ${Utils.formatTimeStamp(lastTs)}")

        val result = mutableListOf<RawSms>()
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
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
                    val id = it.getLong(0)
                    val body = it.getString(1).orEmpty()
                    val date = it.getLong(2)
                    val sender = it.getString(3).orEmpty()

                    val classification = classifier.classify(body, sender)
                    val excludedSender = sender.contains("EPFO", ignoreCase = true)
                    val shouldImport = !excludedSender && classification.isFinancialTransaction

                    AppLogger.d(
                        "SMS_ML_CLASSIFICATION",
                        "time=${Utils.formatTimeStamp(date)}, " +
                            "purpose=${classification.purpose}, " +
                            "direction=${classification.direction}, " +
                            "confidence=${classification.confidence}, " +
                            "financial=${classification.isFinancialTransaction}, " +
                            "excludedSender=$excludedSender, imported=$shouldImport, " +
                            "explanation=${classification.explanation}"
                    )

                    if (shouldImport) {
                        result.add(RawSms(id = id, body = body, timestamp = date, sender = sender))
                    }
                }
            }

            AppLogger.d("SMS_FLOW", "Filtered ${result.size} transaction SMS")

            if (result.isNotEmpty()) {
                SmsSyncStore.saveLastTimestamp(context, result.last().timestamp)
            }
        } catch (_: SecurityException) {
            AppLogger.d("SMS_FLOW", "SecurityException reading SMS inbox; permission missing")
        } catch (e: Exception) {
            AppLogger.e("SMS_FLOW", "Error reading SMS inbox", e)
        }

        result
    }

    suspend fun readSmsBodyById(smsId: Long?): SmsBodyLookupResult =
        withContext(Dispatchers.IO) {
            if (smsId == null) return@withContext SmsBodyLookupResult.NotFound
            if (!SmsPermission.hasPermission(context)) {
                return@withContext SmsBodyLookupResult.PermissionDenied
            }

            try {
                val cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(Telephony.Sms.BODY),
                    "${Telephony.Sms._ID} = ?",
                    arrayOf(smsId.toString()),
                    null
                ) ?: return@withContext SmsBodyLookupResult.NotFound

                cursor.use {
                    if (!it.moveToFirst()) {
                        SmsBodyLookupResult.NotFound
                    } else {
                        val body = it.getString(0).orEmpty()
                        if (body.isBlank()) {
                            SmsBodyLookupResult.NotFound
                        } else {
                            SmsBodyLookupResult.Found(body)
                        }
                    }
                }
            } catch (_: SecurityException) {
                SmsBodyLookupResult.PermissionDenied
            } catch (e: Exception) {
                AppLogger.e("SMS_READER", "Failed to query SMS body", e)
                SmsBodyLookupResult.NotFound
            }
        }
}
