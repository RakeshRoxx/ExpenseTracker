package domain

import kotlinx.serialization.Serializable

@Serializable
data class ClassificationResult(
    val purpose: MessagePurpose,
    val direction: MoneyDirection,
    val confidence: Float,
    val explanation: String? = null
) {
    /**
     * Only actual purchase debits qualify as expenses.
     * Credit card bill settlements, transfers, statements, reminders, and refunds do NOT.
     */
    val isExpense: Boolean
        get() = purpose == MessagePurpose.PURCHASE && direction == MoneyDirection.DEBIT

    /**
     * Whether this message represents an actual completed financial event (debit/credit).
     */
    val isFinancialTransaction: Boolean
        get() = purpose in setOf(
            MessagePurpose.PURCHASE,
            MessagePurpose.CARD_PAYMENT,
            MessagePurpose.TRANSFER,
            MessagePurpose.INCOME,
            MessagePurpose.REFUND
        )
}
