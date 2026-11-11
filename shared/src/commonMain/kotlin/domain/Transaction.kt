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
    val smsId: Long? = null,
    val purpose: MessagePurpose = MessagePurpose.PURCHASE
) {
    /**
     * Only actual purchase debits count as expenses.
     * Credit card bill settlements (CARD_PAYMENT), transfers, statements, and reminders are excluded.
     */
    val isExpense: Boolean
        get() = type == TransactionType.DEBIT && purpose == MessagePurpose.PURCHASE
}
