package com.amaterasu.expense_tracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amaterasu.expense_tracker.data.dao.TransactionDao
import com.amaterasu.expense_tracker.data.entity.TransactionEntity


@Database(
    entities = [TransactionEntity::class],
    version = 4,
    exportSchema = true
)
abstract class RoomDatabase : RoomDatabase() {
    abstract fun TransactionDao(): TransactionDao;
}
