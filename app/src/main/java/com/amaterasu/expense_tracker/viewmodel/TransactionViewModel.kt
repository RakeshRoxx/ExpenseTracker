package com.amaterasu.expense_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amaterasu.expense_tracker.data.database.RoomDatabaseProvider
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.data.repository.TransactionRepository
import com.amaterasu.expense_tracker.ml.TfidfClassifierProvider
import com.amaterasu.expense_tracker.sms.SmsExporter
import com.amaterasu.expense_tracker.usecase.ImportSmsTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId

class TransactionViewModel(app: Application) : AndroidViewModel(app) {

    private val db = RoomDatabaseProvider.get(app)
    private val repo = TransactionRepository(db.TransactionDao())

    private val importUseCase =
        ImportSmsTransactionsUseCase(
            getApplication(),
            llmFallback = { _ ->
                // ❌ LLM disabled for now
                null
            }
        )

    val transactions: StateFlow<List<TransactionEntity>> =
        repo.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _monthlyTotal = MutableStateFlow(0.0)
    val monthlyTotal: StateFlow<Double> = _monthlyTotal

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        refreshMonthlyTotal()

        // 🔥 Warm up ML model in background (prevents first-run ANR)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                TfidfClassifierProvider.get(getApplication())
                Log.d("ML_INIT", "TF-IDF classifier loaded")
            } catch (e: Exception) {
                Log.e("ML_INIT", "Failed to init TF-IDF classifier", e)
            }
        }
    }

    // Recalculate whenever DB changes
    private fun refreshMonthlyTotal() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val start = now.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000

            val end = now.plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000

            _monthlyTotal.value = repo.totalAmountByDate(start, end)
            Log.d("TransactionViewModel", "Total Monthly Expense ${_monthlyTotal.value}")
        }
    }

    fun refreshFromSms() {
        viewModelScope.launch {
            try {
                isRefreshing = true

                // 1️⃣ Parse SMS (rule-based only for now)
                val newTransactions = importUseCase.execute()

                // 2️⃣ Save to Room
                repo.saveAll(newTransactions)

                // 3️⃣ Recalculate totals
                refreshMonthlyTotal()

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Refresh failed", e)
            } finally {
                isRefreshing = false
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repo.updateTransaction(transaction)
                refreshMonthlyTotal()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Update failed", e)
            }
        }
    }

    private fun stableIdFromJson(
        amount: Double,
        merchant: String,
        smsTimestamp: Long,
        type: String
    ): String {
        val raw = "$amount|$merchant|$smsTimestamp|$type"
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun exportSmsForTraining(onDone: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    SmsExporter.exportAllSmsToJsonl(getApplication())
                }
                Log.d("TransactionViewModel", "SMS exported to: ${file.absolutePath}")
                onDone(file.absolutePath)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "SMS export failed", e)
                onDone("FAILED")
            }
        }
    }
}
