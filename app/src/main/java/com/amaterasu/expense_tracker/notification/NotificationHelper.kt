package com.amaterasu.expense_tracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "sms_import_channel";

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transaction Import",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background Import Of Bank Transactions";
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            manager.createNotificationChannel(channel);
        }
    }
}