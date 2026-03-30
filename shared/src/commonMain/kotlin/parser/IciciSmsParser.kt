package parser

import android.util.Log
import domain.Category
import domain.Transaction
import domain.TransactionSource
import domain.TransactionType

class IciciSmsParser : BankSmsParser() {
    override fun canHandle(sms: String): Boolean {
        return sms.contains("ICICI", ignoreCase = true);
    }

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        Log.d("ICICI_SMS_PARSER", sms);

        val amount = extractAmount(sms);

        if (amount == null) {
            Log.d("ICICI_SMS_PARSER", "Failed to parse transaction amount");
            return null;
        }

        val txType = extractTransactionType(sms)
        val merchant = MerchantExtractor.extract(sms, txType) ?: "Unknown"

        val tx = Transaction(
            id = "$timestamp-$amount",
            amount = amount,
            type = txType,
            merchant = merchant,
            smsReceivedTimestamp = timestamp,
            parsingTimestamp = timestamp,
            category = Category.OTHER,
            source = TransactionSource.SMS,
            sourceBank = "ICICI"
        )

        Log.d("ICICI_SMS_PARSER", tx.toString());

        return tx;
    }
}
