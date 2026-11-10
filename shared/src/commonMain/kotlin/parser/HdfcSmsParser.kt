package parser

import domain.Category
import domain.MessagePurpose
import domain.Transaction
import domain.TransactionSource
import domain.TransactionType

class HdfcSmsParser : BankSmsParser() {

    override fun canHandle(sms: String): Boolean = canHandle(sms, "")

    override fun canHandle(sms: String, sender: String): Boolean {
        val senderUpper = sender.uppercase()
        val smsUpper = sms.uppercase()

        if (senderUpper.isNotBlank()) {
            return senderUpper.contains("HDFC")
        }

        return (smsUpper.contains("HDFC BANK") || smsUpper.contains("HDFCBK")) &&
               (smsUpper.contains("A/C") || smsUpper.contains("CARD") ||
                smsUpper.contains("DEBIT") || smsUpper.contains("CREDIT") ||
                smsUpper.contains("SPENT") || smsUpper.contains("USED AT"))
    }

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        // Exclude non-transactional messages (statements, reminders, OTPs, marketing)
        if (isNonTransactional(sms)) {
            return null
        }

        val amount = extractAmount(sms) ?: return null
        val txType = extractTransactionTypeOrNull(sms) ?: return null
        val purpose = resolvePurpose(sms, txType)

        val merchant = MerchantLabelResolver.resolve(sms, txType).displayName
            ?: if (txType == TransactionType.DEBIT) "Bank Debit" else "Bank Credit"

        val category = when (purpose) {
            MessagePurpose.CARD_PAYMENT -> Category.TRANSFER
            MessagePurpose.TRANSFER -> Category.TRANSFER
            else -> Category.OTHER
        }

        return Transaction(
            id = "$timestamp-$amount",
            amount = amount,
            type = txType,
            merchant = merchant,
            smsReceivedTimestamp = timestamp,
            parsingTimestamp = timestamp,
            category = category,
            source = TransactionSource.SMS,
            sourceBank = "HDFC",
            rawSmsBody = sms,
            purpose = purpose
        )
    }
}
