package com.amaterasu.expense_tracker.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TfidfClassifierProvider {

    init {
        Log.d("TfidfClassifierProvider", "Classifier initialized")
    }


    @Volatile
    private var instance: TfidfTransactionClassifier? = null

    suspend fun get(context: Context): TfidfTransactionClassifier {
        // Fast path (no lock)
        instance?.let { return it }

        // Slow path: initialize on IO thread
        val created = withContext(Dispatchers.IO) {
            TfidfTransactionClassifier(context.applicationContext)
        }

        // Publish safely
        synchronized(this) {
            // Another coroutine might have initialized while we were creating
            return instance ?: created.also { instance = it }
        }
    }

    fun isReady(): Boolean = instance != null
}