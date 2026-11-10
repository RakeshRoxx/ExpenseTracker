package com.amaterasu.expense_tracker.logging

import android.util.Log
import com.amaterasu.expense_tracker.BuildConfig

/**
 * App Logcat output is enabled in debug builds and silent in release builds.
 */
object AppLogger {
    private val enabled = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (enabled) Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!enabled) return

        if (throwable == null) {
            Log.e(tag, message)
        } else {
            Log.e(tag, message, throwable)
        }
    }
}
