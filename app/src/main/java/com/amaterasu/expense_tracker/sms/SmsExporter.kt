package com.amaterasu.expense_tracker.sms

import android.content.Context
import android.provider.Telephony
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object SmsExporter {

    fun exportAllSmsToJsonl(context: Context): File {
        Log.d("exportAllSmsToJsonl", "Exporter function called")
        val outputFile = File(
            context.getExternalFilesDir(null),
            "sms_dataset_${System.currentTimeMillis()}.jsonl"
        )

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,   // Inbox + Sent + Drafts
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.DATE,
                Telephony.Sms.BODY,
                Telephony.Sms.TYPE
            ),
            null, null,
            Telephony.Sms.DATE + " DESC"
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        outputFile.bufferedWriter().use { writer ->
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val sender = it.getString(1) ?: ""
                    val timestamp = dateFormat.format(Date(it.getLong(2)))
                    val body = it.getString(3) ?: ""
                    val type = it.getInt(4) // inbox/sent

                    val json = JSONObject().apply {
                        put("id", id)
                        put("address", sender)
                        put("timestamp", timestamp)
                        put("body", anonymize(body))
                        put("type", type)
                    }

                    if(looksLikeTransaction(body, sender)) {
                        writer.write(json.toString())
                        writer.newLine()
                    }
                }
            }
        }

        return outputFile
    }

    // 🔐 Mask sensitive info before exporting
    private fun anonymize(text: String): String {
        return text
            .replace(Regex("\\b\\d{10}\\b"), "XXXXXXXXXX")          // phone numbers
            .replace(Regex("A/c\\s*\\d+"), "A/c XXXXX")             // account numbers
            .replace(Regex("[a-zA-Z0-9._-]+@[a-zA-Z]+"), "user@upi")// UPI IDs
            .replace(Regex("\\b\\d{4,}\\b"), "XXXX")                // long numbers
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