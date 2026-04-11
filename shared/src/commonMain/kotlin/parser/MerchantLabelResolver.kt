package parser

import domain.TransactionType

internal enum class MerchantLabelConfidence {
    HIGH,
    LOW
}

internal enum class MerchantLabelKind {
    MERCHANT,
    GENERIC
}

internal data class MerchantLabelResolution(
    val displayName: String?,
    val confidence: MerchantLabelConfidence,
    val kind: MerchantLabelKind
)

internal object MerchantLabelResolver {

    fun resolve(text: String, transactionType: TransactionType): MerchantLabelResolution {
        MerchantExtractor.extract(text, transactionType)?.let { merchant ->
            return MerchantLabelResolution(
                displayName = merchant,
                confidence = MerchantLabelConfidence.HIGH,
                kind = MerchantLabelKind.MERCHANT
            )
        }

        return MerchantLabelResolution(
            displayName = inferGenericLabel(text, transactionType),
            confidence = MerchantLabelConfidence.LOW,
            kind = MerchantLabelKind.GENERIC
        )
    }

    private fun inferGenericLabel(text: String, transactionType: TransactionType): String {
        val normalized = text.lowercase()

        return when {
            "salary" in normalized && transactionType == TransactionType.CREDIT -> "Salary"
            "refund" in normalized -> "Refund"
            "cash deposit" in normalized -> "Cash Deposit"
            "atm" in normalized && transactionType == TransactionType.DEBIT -> "ATM Withdrawal"
            "nach" in normalized -> "NACH"
            "imps" in normalized -> "IMPS Transfer"
            "neft" in normalized -> "NEFT Transfer"
            "rtgs" in normalized -> "RTGS Transfer"
            Regex("""\bupi(?:/p2[am])?\b""").containsMatchIn(normalized) -> {
                if (transactionType == TransactionType.CREDIT) "UPI Credit" else "UPI Transfer"
            }
            transactionType == TransactionType.DEBIT &&
                (
                    "used at" in normalized ||
                        Regex("""\b(?:credit|debit)\s+card\b""").containsMatchIn(normalized) ||
                        Regex("""\bcard\b""").containsMatchIn(normalized)
                    ) -> "Card Spend"
            transactionType == TransactionType.DEBIT -> "Bank Debit"
            else -> "Bank Credit"
        }
    }
}
