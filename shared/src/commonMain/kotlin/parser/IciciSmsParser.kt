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

        val merchant = Regex("at\\s([A-Z0-9 ]+)")
            .find(sms)?.groupValues?.get(1)
            ?: "Unknown"

        val tx = Transaction(
            id = "$timestamp-$amount",
            amount = amount,
            type = TransactionType.DEBIT,
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