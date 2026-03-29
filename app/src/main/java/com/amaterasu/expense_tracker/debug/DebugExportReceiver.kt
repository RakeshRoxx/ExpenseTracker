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
        Log.d("DebugExportReceiver", "🔥 onReceive called. action=${intent.action}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DebugExportReceiver", "⏳ Starting SMS export...")
                val file = SmsExporter.exportAllSmsToJsonl(context)
                Log.d("DebugExportReceiver", "✅ Exported SMS to: ${file.absolutePath}")
            } catch (t: Throwable) {
                Log.e("DebugExportReceiver", "❌ Export failed", t)
            }
        }
    }
}