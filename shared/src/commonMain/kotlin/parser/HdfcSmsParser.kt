package parser

import android.util.Log
import domain.Category
import domain.Transaction
import domain.TransactionSource
import domain.TransactionType

class HdfcSmsParser : BankSmsParser() {

    override fun canHandle(sms: String): Boolean =
        sms.contains("HDFC", ignoreCase = true)

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        Log.d("HDFC_SMS_PARSER", sms);

        val amount = extractAmount(sms) ?: return null
        val txType = extractTransactionType(sms)
        val merchant = MerchantExtractor.extract(sms, txType) ?: "Unknown"

        return Transaction(
            id = "$timestamp-$amount",
            amount = amount,
            type = txType,
            merchant = merchant,
            smsReceivedTimestamp = timestamp,
            parsingTimestamp = timestamp,
            category = Category.OTHER,
            source = TransactionSource.SMS,
            sourceBank = "HDFC",
            rawSmsBody = sms
        )
    }
}
