package com.amaterasu.expense_tracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions : List<TransactionEntity>);

    @Query("SELECT * FROM transactions ORDER BY smsReceivedTimestamp DESC")
    fun observeAll() : Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("""SELECT IFNULL(SUM(amount), 0) 
        FROM transactions
        WHERE type = 'DEBIT'
        AND smsReceivedTimestamp BETWEEN :start and :end""")
    suspend fun totalSpendByDate(start: Long, end: Long) : Double

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
}
