package parser

import classifier.RuleBasedSmsClassifier
import domain.ClassificationResult
import domain.MessagePurpose
import domain.MoneyDirection
import domain.TransactionType

abstract class BankSmsParser : SmsParser {

    protected val intentClassifier = RuleBasedSmsClassifier()

    /**
     * Checks if this message is non-transactional (e.g. statement, due reminder, OTP, or promo).
     * Non-transactional messages must NEVER produce a Transaction entity.
     */
    protected fun isNonTransactional(sms: String): Boolean {
        val result = intentClassifier.classify(sms)
        return !result.isFinancialTransaction
    }

    /**
     * Classifies the message intent and direction.
     */
    protected fun classify(sms: String, sender: String = ""): ClassificationResult {
        return intentClassifier.classify(sms, sender)
    }

    /**
     * Resolves the message purpose (PURCHASE, CARD_PAYMENT, TRANSFER, INCOME, REFUND, etc.).
     */
    protected fun resolvePurpose(sms: String, txType: TransactionType): MessagePurpose {
        val result = intentClassifier.classify(sms)
        return when (result.purpose) {
            MessagePurpose.CARD_PAYMENT -> MessagePurpose.CARD_PAYMENT
            MessagePurpose.REFUND -> MessagePurpose.REFUND
            MessagePurpose.INCOME -> MessagePurpose.INCOME
            MessagePurpose.TRANSFER -> MessagePurpose.TRANSFER
            MessagePurpose.PURCHASE -> MessagePurpose.PURCHASE
            else -> if (txType == TransactionType.DEBIT) MessagePurpose.PURCHASE else MessagePurpose.INCOME
        }
    }

    /**
     * Robust amount extraction:
     * 1. Prioritizes amounts tied to transactional action keywords (debited, spent, paid, credited).
     * 2. Rejects amounts preceded by balance markers (avl bal, available balance) or due markers
     *    (minimum amount due, total due).
     */
    protected fun extractAmount(text: String): Double? {
        // Step 1: Action-verb contextual match
        val contextualActionRegex = Regex(
            """(?:debited\s+(?:by|with|for)?|spent|withdrawn|paid|credited\s+(?:with|by)?|refunded?\s+(?:of)?|received)\s*(?:INR|Rs[\.,]?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
        contextualActionRegex.find(text)?.let { match ->
            match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()?.let { return it }
        }

        // Step 2: Amount preceding action verb (e.g. "INR 500 debited")
        val prefixActionRegex = Regex(
            """(?:INR|Rs[\.,]?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:debited|credited|spent|withdrawn|paid|received|refunded)""",
            RegexOption.IGNORE_CASE
        )
        prefixActionRegex.find(text)?.let { match ->
            match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()?.let { return it }
        }

        // Step 3: Scan all currency patterns, skipping balance and statement/due totals
        val allAmountsRegex = Regex(
            """(?:INR|Rs[\.,]?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
        val exclusionPrefixes = listOf(
            "avl bal", "avail bal", "available bal", "bal:", "balance:", "bal is", "balance is",
            "total due", "min due", "minimum due", "minimum amount due", "total amount due",
            "credit limit", "available limit", "limit:", "limit is", "points"
        )

        for (match in allAmountsRegex.findAll(text)) {
            val startIdx = match.range.first
            val prefixSlice = text.substring(0.coerceAtLeast(startIdx - 35), startIdx).lowercase()
            val isExcluded = exclusionPrefixes.any { prefixSlice.contains(it) }
            if (!isExcluded) {
                val value = match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
                if (value != null && value > 0.0) {
                    return value
                }
            }
        }

        // Fallback: first amount
        return allAmountsRegex.find(text)
            ?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
    }

    /**
     * Extracts transaction direction (DEBIT or CREDIT) with positive evidence.
     * Returns null if ambiguous, preventing blind defaulting to DEBIT.
     */
    protected fun extractTransactionTypeOrNull(text: String): TransactionType? {
        val result = intentClassifier.classify(text)
        return when (result.direction) {
            MoneyDirection.DEBIT -> TransactionType.DEBIT
            MoneyDirection.CREDIT -> TransactionType.CREDIT
            else -> null
        }
    }

    /**
     * Backwards-compatible overload.
     */
    protected fun extractTransactionType(text: String): TransactionType {
        return extractTransactionTypeOrNull(text) ?: TransactionType.DEBIT
    }
}
