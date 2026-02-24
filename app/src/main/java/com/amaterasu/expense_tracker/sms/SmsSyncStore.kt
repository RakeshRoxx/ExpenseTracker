package com.amaterasu.expense_tracker.sms

import android.content.Context
import androidx.core.content.edit

object SmsSyncStore {
    private const val PREF = "sms_sync";
    private const val KEY_LAST_TS = "last_sms_ts";

    fun getLastTimestamp(context: Context): Long {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getLong(KEY_LAST_TS, 0L);
    }

    fun saveLastTimestamp(context: Context, ts: Long) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit {
                putLong(KEY_LAST_TS, ts)
            };
    }
}