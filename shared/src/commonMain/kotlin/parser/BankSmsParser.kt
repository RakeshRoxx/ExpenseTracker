package parser

import domain.TransactionType

abstract class BankSmsParser : SmsParser {

    protected fun extractAmount(text: String): Double? {
        val regex = Regex(
            "(?:INR|Rs[\\.,]?|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
            RegexOption.IGNORE_CASE
        )

        val match = regex.find(text)
        val raw = match?.groupValues?.get(1)

        return raw
            ?.replace(",", "")
            ?.toDoubleOrNull()
    }

    protected fun extractTransactionType(text: String): TransactionType {
        val normalized = text.lowercase()
        val debitPatterns = listOf(
            "\\bdebited\\b",
            "\\bdebit\\b",
            "\\bdr\\.?\\b",
            "\\bspent\\b",
            "\\bwithdrawn\\b"
        )
        val creditPatterns = listOf(
            "\\bcredited\\b",
            "\\bcredited\\s+to\\b",
            "\\bcredited\\s+with\\b",
            "\\bcredit(?:ed)?\\s+of\\b",
            "\\bcr\\.?\\b",
            "\\breceived\\b"
        )

        return when {
            debitPatterns.any { Regex(it).containsMatchIn(normalized) } ->
                TransactionType.DEBIT

            creditPatterns.any { Regex(it).containsMatchIn(normalized) } ->
                TransactionType.CREDIT

            else -> TransactionType.DEBIT
        }
    }
}
