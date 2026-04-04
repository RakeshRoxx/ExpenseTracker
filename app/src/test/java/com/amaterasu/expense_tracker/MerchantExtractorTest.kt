package com.amaterasu.expense_tracker

import domain.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import parser.MerchantExtractor

class MerchantExtractorTest {

    @Test
    fun extractsMerchantFromCardUsageSms() {
        val sms = "Your HSBC credit card xxxxx8963 is used at bigbasket for INR 934.35 on 08/05/25."

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertEquals("BigBasket", merchant)
    }

    @Test
    fun extractsMerchantFromUpiHandleStyleSms() {
        val sms = "ICICI Bank Credit Card XX1006 debited for INR 90.00 on 23-Feb-26 for UPI-XXXX-Fatima. To dispute call XXXX"

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertEquals("Fatima", merchant)
    }

    @Test
    fun extractsMerchantFromUpiPathSms() {
        val sms = "INR 20.00 debited A/c no. XX0034 UPI/P2M/XXXX/DMART Not you? SMS BLOCKUPI"

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertEquals("DMart", merchant)
    }

    @Test
    fun extractsMerchantFromIncomingTransactionSms() {
        val sms = "Rs. 428 received in a/c xx7567 on 13-Feb-25 from VINEET KUMAR (UPI Ref: XXXX)"

        val merchant = MerchantExtractor.extract(sms, TransactionType.CREDIT)

        assertEquals("VINEET KUMAR", merchant)
    }

    @Test
    fun handlesIncomingTransactionWithMultipleTrailingStopPhrases() {
        val sms = "Rs. 428 received in a/c xx7567 on 13-Feb-25 from VINEET KUMAR (UPI Ref: XXXX) on card xx12 - slice(NESFB)"

        val merchant = MerchantExtractor.extract(sms, TransactionType.CREDIT)

        assertEquals("VINEET KUMAR", merchant)
    }

    @Test
    fun doesNotTreatBillReminderAsMerchant() {
        val sms = "Payment of INR XXXX.33 for Axis Bank Credit Card no. XX7215 is due on 02-03-26 with minimum amount due of INR 957."

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertNull(merchant)
    }

    @Test
    fun doesNotTreatUpiHandleAsMerchant() {
        val sms = "Rs. 340 sent to 9988776655@oksbi on 21-Mar-26. UPI Ref 123456789"

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertNull(merchant)
    }

    @Test
    fun doesNotTreatMixedAlphaNumericReferenceAsMerchant() {
        val sms = "ICICI Bank Credit Card XX1006 debited for INR 90.00 on 23-Feb-26 for UPI-XXXX-AB12CD34. To dispute call XXXX"

        val merchant = MerchantExtractor.extract(sms, TransactionType.DEBIT)

        assertNull(merchant)
    }
}
