package parser

import domain.Transaction

interface SmsParser {
    fun canHandle(sms: String): Boolean = canHandle(sms, "")
    fun canHandle(sms: String, sender: String): Boolean = canHandle(sms)
    fun parse(sms: String, timestamp: Long, sender: String): Transaction?
}