package parser

import domain.Transaction

class DefaultSMSParser : BankSmsParser() {
    override fun canHandle(sms: String): Boolean {
        return true;
    }

    override fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        TODO("Not yet implemented")
    }
}