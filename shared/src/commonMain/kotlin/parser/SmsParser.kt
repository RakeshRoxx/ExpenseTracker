package parser

import domain.Transaction

interface SmsParser {
    fun canHandle(sms: String): Boolean
    fun parse(sms: String, timestamp: Long, sender: String): Transaction?
}