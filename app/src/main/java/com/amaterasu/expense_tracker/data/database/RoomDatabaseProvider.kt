package com.amaterasu.expense_tracker.data.database

import android.content.Context
import androidx.room.Room

object RoomDatabaseProvider {

    @Volatile
    private var INSTANCE: RoomDatabase? = null;

    fun get(context: Context): RoomDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                RoomDatabase::class.java,
                "expense_tracker.db"
            )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }

}