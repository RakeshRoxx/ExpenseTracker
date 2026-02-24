package com.amaterasu.expense_tracker.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.amaterasu.expense_tracker.sms.SmsExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugExportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.amaterasu.expense_tracker.EXPORT_SMS") {
            CoroutineScope(Dispatchers.IO).launch {
                val file = SmsExporter.exportAllSmsToJsonl(context)
                Log.d("DebugExportReceiver", "Exported: ${file.absolutePath}")
            }
        }
    }
}