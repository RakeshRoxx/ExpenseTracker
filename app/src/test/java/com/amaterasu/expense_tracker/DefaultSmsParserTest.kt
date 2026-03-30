package com.amaterasu.expense_tracker

import domain.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import parser.DefaultSMSParser

class DefaultSmsParserTest {

    private val parser = DefaultSMSParser()

    @Test
    fun parsesOtherBankDebitSms() {
        val sms = "Dear Customer, Your A/C XXXXX995321 has a debit by NACH of Rs 2,000.00 on 26/10/24. Avl Bal Rs 12,788.43. Download YONO - SBI"

        val result = parser.parse(sms, 1710000000000, "VK-SBIINB-S")

        assertNotNull(result)
        assertEquals(2000.0, result?.amount ?: 0.0, 0.0)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals("NACH", result?.merchant)
        assertEquals("SBI", result?.sourceBank)
    }

    @Test
    fun parsesOtherBankCreditSms() {
        val sms = "Rs. 428 received in a/c xx7567 on 13-Feb-25 from VINEET KUMAR (UPI Ref: XXXX) - slice(NESFB)"

        val result = parser.parse(sms, 1710000000000, "VM-SLICE-S")

        assertNotNull(result)
        assertEquals(428.0, result?.amount ?: 0.0, 0.0)
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals("VINEET KUMAR", result?.merchant)
        assertEquals("SLICE", result?.sourceBank)
    }

    @Test
    fun ignoresStatementMessages() {
        val sms = "Your credit card bill for BOBCARD XXXX-XXXX has been generated. Total amount: INR 548.00. Due date: February 04, XXXX."

        val result = parser.parse(sms, 1710000000000, "VM-BOBCRD-S")

        assertNull(result)
    }
}
