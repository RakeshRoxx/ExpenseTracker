package parser

import domain.Transaction

class SmsParserEngine(
    private val parsers: List<SmsParser>
) {
    fun parse(sms: String, timestamp: Long, sender: String): Transaction? {
        val parser = parsers.firstOrNull { it.canHandle(sms) }
        return parser?.parse(sms, timestamp, sender)
    }
}