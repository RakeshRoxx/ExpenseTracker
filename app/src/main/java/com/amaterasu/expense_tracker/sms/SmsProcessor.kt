package com.amaterasu.expense_tracker.sms

import domain.Transaction
import parser.AxisSmsParser
import parser.HdfcSmsParser
import parser.IciciSmsParser
import parser.SmsParserEngine

class SmsProcessor {

    private val engine = SmsParserEngine(
        listOf(
            HdfcSmsParser(),
            AxisSmsParser(),
            IciciSmsParser()
        )
    )

    fun parseOne(raw: RawSms): Transaction? {
        return engine.parse(raw.body, raw.timestamp, raw.sender)
    }

    fun process(smsList: List<RawSms>): List<Transaction> {
        return smsList.mapNotNull {
            engine.parse(it.body, it.timestamp, it.sender)
        }
    }
}