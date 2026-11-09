package com.amaterasu.expense_tracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `colorIndex` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

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
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }

}