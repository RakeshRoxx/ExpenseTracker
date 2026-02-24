package com.amaterasu.expense_tracker.sms

import android.content.Context
import android.provider.Telephony
import android.util.Log
import utils.Utils

class SmsReader(private val context: Context) {

    fun readInbox(): List<RawSms> {
        Log.d("SMS_FLOW", "Reading SMS inbox");

        val lastTs = SmsSyncStore.getLastTimestamp(context);

        Log.d("SMS_FLOW", "Last Ts ${Utils.formatTimeStamp(lastTs)}");

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
            Log.d("SMS_FLOW", "SMS cursor count = ${it.count}")

            while (it.moveToNext()) {
                val body = it.getString(0)
                val date = it.getLong(1)
                val sender = it.getString(2)

                if (looksLikeTransaction(body, sender)) {
                    result.add(RawSms(body, date, sender))
                }
            }
        }
        Log.d("SMS_FLOW", "Filtered ${result.size} transaction SMS")

        if (result.isNotEmpty()) {
            SmsSyncStore.saveLastTimestamp(context, result.last().timestamp);
        }

        return result
    }

    private fun looksLikeTransaction(text: String, sender: String): Boolean {
        Log.d("looksLikeTransaction", "Sender Name: $sender");
        Log.d("looksLikeTransaction", "Sms Body: $text");

        if (sender.contains("-S") || sender.contains("-T")) {
            return true;
        }

        return listOf("debit", "debited", "credit", "credited", "spent", "₹", "rs.", "INR")
            .any { text.contains(it, ignoreCase = true) };
    }
}