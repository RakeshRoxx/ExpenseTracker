package com.amaterasu.expense_tracker

import domain.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import parser.DefaultSMSParser
import parser.HdfcSmsParser

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

    @Test
    fun usesGenericLabelForLowConfidenceUpiDebit() {
        val sms = "Rs. 340 sent to 9988776655@oksbi on 21-Mar-26. UPI Ref 123456789"

        val result = parser.parse(sms, 1710000000000, "VK-SBIINB-S")

        assertNotNull(result)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals("UPI Transfer", result?.merchant)
    }

    @Test
    fun usesGenericLabelForLowConfidenceUpiCredit() {
        val sms = "Rs. 428 received in a/c xx7567 on 13-Feb-25 from 9988776655@oksbi (UPI Ref: XXXX)"

        val result = parser.parse(sms, 1710000000000, "VM-SLICE-S")

        assertNotNull(result)
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals("UPI Credit", result?.merchant)
    }

    @Test
    fun usesFallbackLabelForAmbiguousDebitWithoutMerchant() {
        val sms = "Your account XX1234 has been debited by INR 120.00 on 21-Mar-26. Avl bal INR 850.00"

        val result = parser.parse(sms, 1710000000000, "VM-TEST-S")

        assertNotNull(result)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals("Bank Debit", result?.merchant)
    }

    @Test
    fun usesFallbackLabelForAmbiguousCreditWithoutMerchant() {
        val sms = "INR 120.00 credited to your account XX1234 on 21-Mar-26. Avl bal INR 850.00"

        val result = parser.parse(sms, 1710000000000, "VM-TEST-S")

        assertNotNull(result)
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals("Bank Credit", result?.merchant)
    }

    @Test
    fun bankSpecificParserUsesCleanGenericLabelInsteadOfUnknown() {
        val parser = HdfcSmsParser()
        val sms = "Your HDFC Bank credit card XX1234 is used at POS for INR 499.00 on 08/05/25."

        val result = parser.parse(sms, 1710000000000, "VM-HDFCBK-S")

        assertNotNull(result)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals("Card Spend", result?.merchant)
    }
}
