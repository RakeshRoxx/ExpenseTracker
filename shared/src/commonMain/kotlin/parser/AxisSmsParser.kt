package parser

import android.util.Log
import domain.Category
import domain.Transaction
import domain.TransactionSource
import domain.TransactionType
import utils.Utils

class AxisSmsParser : BankSmsParser() {

    override fun canHandle(sms: String): Boolean =
        sms.contains("AXIS", ignoreCase = true)

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        Log.d("AXIS_SMS_PARSER", sms);

//        val smsReceivedTimestamp = sms.get

        val amount = extractAmount(sms);

        if (amount == null) {
            Log.d("AXIS_SMS_PARSER", "Failed to parse transaction amount");
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
            parsingTimestamp = System.currentTimeMillis(),
            category = Category.OTHER,
            source = TransactionSource.SMS,
            sourceBank = "AXIS"
        )

        val formatedDate = Utils.formatTimeStamp(timestamp);

        Log.d("AXIS_PARSER", "SMS_DATE $formatedDate")
        Log.d("AXIS_PARSER", "SMS_SENDER $sender")
        Log.d("AXIS_PARSER", tx.toString());

        return tx;
    }
}
