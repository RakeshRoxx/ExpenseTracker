package domain

import kotlinx.serialization.Serializable

@Serializable
data class Transaction (
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val smsReceivedTimestamp: Long,
    val parsingTimestamp: Long,
    val category: Category,
    val source: TransactionSource,
    val accountHint: String? = null,
    val sourceBank: String,
    val rawSmsBody: String? = null
)
