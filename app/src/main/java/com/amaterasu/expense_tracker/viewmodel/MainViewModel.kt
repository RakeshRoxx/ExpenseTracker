package com.amaterasu.expense_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amaterasu.expense_tracker.data.database.RoomDatabaseProvider
import com.amaterasu.expense_tracker.data.repository.TransactionRepository
import com.amaterasu.expense_tracker.usecase.ImportSmsTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel (app: Application) : AndroidViewModel(app) {
    private val importUseCase = ImportSmsTransactionsUseCase(app.applicationContext,);

    // Database setup

    private val db = RoomDatabaseProvider.get(app);
    private val repository = TransactionRepository(db.TransactionDao());

    fun importSmsTransaction() {
        viewModelScope.launch (Dispatchers.IO) {
            Log.d("SMS_FLOW", "Import started");

            val transactions = importUseCase.execute();

            Log.d("SMS_FLOW_TRANSACTION_COUNT", "Parsed ${transactions.size} transactions")

            repository.saveAll(transactions);

            Log.d("SAVE_TO_DB", "Saved to DB");

            val count = db.TransactionDao().count();
            Log.d("ROOM_DB", "Transaction count = $count")

//            transactions.forEach {
//                Log.d("SMS_FLOW_TRANSACTION", it.toString());
//            }
        }
    }
}