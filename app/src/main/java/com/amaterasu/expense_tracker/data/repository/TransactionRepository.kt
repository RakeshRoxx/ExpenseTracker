package com.amaterasu.expense_tracker.data.repository

import com.amaterasu.expense_tracker.data.dao.TransactionDao
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.data.mapper.toEntity
import domain.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository (private val dao: TransactionDao) {
    suspend fun saveAll(transaction: List<Transaction>) {
        dao.insertAll(transaction.map { it.toEntity() })
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

    suspend fun totalAmountByDate(start: Long, end: Long) : Double {
        return dao.totalSpendByDate(start, end);
    }

    fun observeAll() : Flow<List<TransactionEntity>>{
        return dao.observeAll();
    }
}
