package com.amaterasu.expense_tracker

import com.amaterasu.expense_tracker.data.mapper.toEntity
import com.amaterasu.expense_tracker.sms.RawSms
import com.amaterasu.expense_tracker.sms.SmsProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionMappingAndSmsIdTest {

    private val processor = SmsProcessor()

    @Test
    fun parseOne_propagatesSmsId_andMapperPreservesItWithoutSmsBody() {
        val rawSms = RawSms(
            id = 42L,
            body = "Dear Customer, INR 500.00 debited from A/C XX1234 on 10-Oct-24 towards SWIGGY. Avl Bal INR 1,200.00 - HDFC Bank",
            timestamp = 1728550000000L,
            sender = "AD-HDFCBK"
        )

        val transaction = processor.parseOne(rawSms)
        assertNotNull(transaction)
        assertEquals(42L, transaction?.smsId)

        val entity = transaction!!.toEntity()
        assertEquals(42L, entity.smsId)
        assertEquals(500.0, entity.amount, 0.0)

        // Ensure TransactionEntity class has no smsBody / originalSms field
        val fields = entity::class.java.declaredFields.map { it.name }
        assert(fields.contains("smsId"))
        assert(!fields.contains("smsBody"))
        assert(!fields.contains("originalSms"))
        assert(!fields.contains("body"))
    }

    @Test
    fun manualTransaction_hasNullSmsId() {
        val entity = com.amaterasu.expense_tracker.data.entity.TransactionEntity(
            id = "manual-1",
            amount = 150.0,
            type = "DEBIT",
            merchant = "Coffee",
            smsReceivedTimestamp = System.currentTimeMillis(),
            parsingTimestamp = System.currentTimeMillis(),
            category = "FOOD",
            source = "MANUAL",
            accountHint = null,
            sourceBank = "Manual",
            smsId = null
        )

        assertNull(entity.smsId)
    }
}
