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

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transactions_new` (
                `id` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `type` TEXT NOT NULL,
                `merchant` TEXT NOT NULL,
                `smsReceivedTimestamp` INTEGER NOT NULL,
                `parsingTimestamp` INTEGER NOT NULL,
                `category` TEXT NOT NULL,
                `source` TEXT NOT NULL,
                `accountHint` TEXT,
                `sourceBank` TEXT NOT NULL,
                `smsId` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `transactions_new` (
                `id`, `amount`, `type`, `merchant`, `smsReceivedTimestamp`,
                `parsingTimestamp`, `category`, `source`, `accountHint`,
                `sourceBank`, `smsId`
            )
            SELECT
                `id`, `amount`, `type`, `merchant`, `smsReceivedTimestamp`,
                `parsingTimestamp`, `category`, `source`, `accountHint`,
                `sourceBank`, NULL
            FROM `transactions`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `transactions`")
        db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
    }
}

object RoomDatabaseProvider {

    @Volatile
    private var INSTANCE: RoomDatabase? = null

    fun get(context: Context): RoomDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                RoomDatabase::class.java,
                "expense_tracker.db"
            )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
