package com.amaterasu.expense_tracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amaterasu.expense_tracker.data.dao.CategoryDao
import com.amaterasu.expense_tracker.data.dao.TransactionDao
import com.amaterasu.expense_tracker.data.entity.CategoryEntity
import com.amaterasu.expense_tracker.data.entity.TransactionEntity


@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 6,
    exportSchema = true
)
abstract class RoomDatabase : RoomDatabase() {
    abstract fun TransactionDao(): TransactionDao
    abstract fun CategoryDao(): CategoryDao
}
