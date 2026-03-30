package com.amaterasu.expense_tracker.sms

import android.content.Context
import androidx.core.content.edit
import com.amaterasu.expense_tracker.BuildConfig
import java.util.Calendar
import java.util.TimeZone

object SmsSyncStore {
    private const val PREF = "sms_sync";
    private const val KEY_LAST_TS = "last_sms_ts";

    fun getLastTimestamp(context: Context): Long {
        if (BuildConfig.TEST_REIMPORT_ON_LAUNCH) {
            return currentMonthStartInIst()
        }

        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getLong(KEY_LAST_TS, 0L);
    }

    fun saveLastTimestamp(context: Context, ts: Long) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit {
                putLong(KEY_LAST_TS, ts)
            };
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit {
                remove(KEY_LAST_TS)
            }
    }

    private fun currentMonthStartInIst(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
