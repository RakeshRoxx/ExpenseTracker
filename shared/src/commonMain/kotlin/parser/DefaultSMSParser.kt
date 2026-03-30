package parser

import domain.Category
import domain.Transaction
import domain.TransactionSource
import domain.TransactionType

class DefaultSMSParser : BankSmsParser() {
    override fun canHandle(sms: String): Boolean {
        return true
    }

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        val amount = extractAmount(sms) ?: return null
        if (!isLikelyTransactionSms(sms)) return null

        val transactionType = extractTransactionType(sms)
        val merchant = MerchantExtractor.extract(sms, transactionType)
            ?: inferNarrationMerchant(sms, transactionType)
            ?: "Unknown"

        return Transaction(
            id = "$timestamp-$amount-$sender",
            amount = amount,
            type = transactionType,
            merchant = merchant,
            smsReceivedTimestamp = timestamp,
            parsingTimestamp = System.currentTimeMillis(),
            category = Category.OTHER,
            source = TransactionSource.SMS,
            accountHint = extractAccountHint(sms),
            sourceBank = inferSourceBank(sender, sms),
            rawSmsBody = sms
        )
    }

    private fun isLikelyTransactionSms(sms: String): Boolean {
        val normalized = sms.lowercase()

        val negativePatterns = listOf(
            "\\botp\\b",
            "\\bstatement\\s+generated\\b",
            "\\bbill\\s+(?:is\\s+)?generated\\b",
            "\\bminimum\\s+amount\\s+due\\b",
            "\\bdue\\s+date\\b",
            "\\bpayment\\s+due\\b",
            "\\bpre-?approved\\b",
            "\\bloan\\s+offer\\b",
            "\\bcredit\\s+limit\\b",
            "\\bavailable\\s+limit\\b",
            "\\breward\\s+points?\\b",
            "\\bcashback\\b.*\\bnext\\s+statement\\b",
            "\\boffer\\b",
            "\\bsale\\b",
            "\\bpromo\\b",
            "\\bvoucher\\b"
        )
        if (negativePatterns.any { Regex(it).containsMatchIn(normalized) }) {
            return false
        }

        val positivePatterns = listOf(
            "\\bdebited\\b",
            "\\bdebit\\b",
            "\\bcredited\\b",
            "\\bcredit(?:ed)?\\s+of\\b",
            "\\breceived\\b",
            "\\bspent\\b",
            "\\bwithdrawn\\b",
            "\\bpaid\\b",
            "\\brefund(?:ed)?\\b",
            "\\bsent\\b",
            "\\bdr\\.?\\b",
            "\\bcr\\.?\\b",
            "\\bnach\\b",
            "\\bimps\\b",
            "\\bneft\\b",
            "\\brtgs\\b",
            "\\bupi(?:/p2[am])?\\b",
            "\\bused\\s+at\\b",
            "\\btxn\\s+id\\b",
            "\\bsalary\\b.*\\bcredited\\b"
        )

        return positivePatterns.any { Regex(it).containsMatchIn(normalized) }
    }

    private fun inferNarrationMerchant(sms: String, transactionType: TransactionType): String? {
        val normalized = sms.lowercase()

        return when {
            "atm" in normalized && transactionType == TransactionType.DEBIT -> "ATM Withdrawal"
            "cash deposit" in normalized -> "Cash Deposit"
            "salary" in normalized && transactionType == TransactionType.CREDIT -> "Salary"
            "refund" in normalized -> "Refund"
            "nach" in normalized -> "NACH"
            "upi" in normalized -> "UPI Transfer"
            "imps" in normalized -> "IMPS Transfer"
            "neft" in normalized -> "NEFT Transfer"
            "rtgs" in normalized -> "RTGS Transfer"
            "emi" in normalized || "autopay" in normalized -> "EMI/Autopay"
            else -> null
        }
    }

    private fun extractAccountHint(sms: String): String? {
        val patterns = listOf(
            Regex("""\b(?:a/c|account)\s*(?:no\.?|ending|xx)?\s*([xX*\d]{3,})""", RegexOption.IGNORE_CASE),
            Regex("""\bcard(?:\s+no\.?|\s+ending)?\s*([xX*\d]{3,})""", RegexOption.IGNORE_CASE)
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(sms)?.groupValues?.getOrNull(1)?.trim()
        }
    }

    private fun inferSourceBank(sender: String, sms: String): String {
        val senderUpper = sender.uppercase()
        val textUpper = sms.uppercase()

        val knownBanks = linkedMapOf(
            "SBI" to listOf("SBI", "SBIINB", "YONO"),
            "YES BANK" to listOf("YES BANK", "YESBNK"),
            "HDFC" to listOf("HDFC"),
            "ICICI" to listOf("ICICI"),
            "AXIS" to listOf("AXIS"),
            "BOB" to listOf("BOB", "BOBCARD", "BANK OF BARODA"),
            "KOTAK" to listOf("KOTAK"),
            "IDFC FIRST" to listOf("IDFC", "FIRST BANK"),
            "INDUSIND" to listOf("INDUSIND"),
            "AIRTEL PAYMENTS BANK" to listOf("AIRTEL PAYMENTS BANK", "AIRBNK"),
            "SLICE" to listOf("SLICE", "NESFB")
        )

        for ((bank, markers) in knownBanks) {
            if (markers.any { marker -> marker in senderUpper || marker in textUpper }) {
                return bank
            }
        }

        return sender
            .trim()
            .ifBlank { "GENERIC" }
    }
}
