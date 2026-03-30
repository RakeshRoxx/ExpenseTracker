package com.amaterasu.expense_tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity (
    @PrimaryKey
    val id: String,

    val amount: Double,
    val type: String,
    val merchant: String,
    val smsReceivedTimestamp: Long,
    val parsingTimestamp: Long,
    val category: String,
    val source: String,
    val accountHint: String?,
    val sourceBank: String,
    val rawSmsBody: String? = null
)
