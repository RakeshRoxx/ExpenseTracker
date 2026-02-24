package domain

import kotlin.compareTo

object TransactionValidator {

    fun isValid(tx: Transaction): Boolean {
        return tx.amount > 0 &&
                tx.merchant.isNotBlank() &&
                tx.smsReceivedTimestamp > 0
    }
}