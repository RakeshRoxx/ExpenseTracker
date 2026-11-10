package com.amaterasu.expense_tracker

import android.content.Context
import android.content.ContextWrapper
import com.amaterasu.expense_tracker.ml.LiteRtTransactionClassifier
import domain.MessagePurpose
import domain.MoneyDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LiteRtClassifierUnitTest {

    private lateinit var stubContext: Context
    private lateinit var classifier: LiteRtTransactionClassifier

    @Before
    fun setup() {
        stubContext = ContextWrapper(null)
        classifier = LiteRtTransactionClassifier(stubContext)
    }

    @Test
    fun tokenization_matchesExactPreprocessingContract() {
        val sms = "INR 1,250.00 spent on HDFC Bank Card XX4321 at ZOMATO! Bal: Rs. 45,000"
        val tokens = classifier.tokenize(sms)

        assertTrue(tokens.contains("inr"))
        assertTrue(tokens.contains("1"))
        assertTrue(tokens.contains("250.00"))
        assertTrue(tokens.contains("spent"))
        assertTrue(tokens.contains("hdfc"))
        assertTrue(tokens.contains("card"))
        assertTrue(tokens.contains("zomato"))
        assertFalse(tokens.contains("zomato!"))
    }

    @Test
    fun realLiteRtModel_runsInferenceOnExportedTfliteModel() {
        // Test real inference with the exported TFLite model
        val sms = "INR 1,250.00 spent on your HDFC Bank Credit Card ending 4321 at ZOMATO on 10-Nov-26. Avl limit: INR 85,000.00."
        val result = classifier.classify(sms, "VM-HDFCBK-S")

        assertNotNull(result)
        assertEquals(MessagePurpose.PURCHASE, result.purpose)
        assertEquals(MoneyDirection.DEBIT, result.direction)
        assertTrue(result.isExpense)
        assertTrue(result.isFinancialTransaction)
        assertTrue("Confidence should be high (>0.80)", result.confidence >= 0.80f)
    }

    @Test
    fun safeFallback_classifiesCardPurchaseAsExpense() {
        val sms = "INR 850.00 spent on your HDFC Bank Credit Card XX4321 at SWIGGY on 10-Nov-26"
        val result = classifier.classify(sms, "VM-HDFCBK-S")

        assertEquals(MessagePurpose.PURCHASE, result.purpose)
        assertEquals(MoneyDirection.DEBIT, result.direction)
        assertTrue(result.isExpense)
        assertTrue(result.isFinancialTransaction)
    }

    @Test
    fun safeFallback_classifiesStatementAsNonTransaction() {
        val sms = "Your HDFC Bank Credit Card statement of Rs. 45,000.00 is generated. Due date 20-Nov-26"
        val result = classifier.classify(sms, "VM-HDFCBK-S")

        assertEquals(MessagePurpose.STATEMENT, result.purpose)
        assertEquals(MoneyDirection.NONE, result.direction)
        assertFalse(result.isExpense)
        assertFalse(result.isFinancialTransaction)
    }

    @Test
    fun safeFallback_classifiesDueReminderAsNonTransaction() {
        val sms = "Reminder: Payment of Rs. 12,000.00 on your Axis Bank Card XX7215 is due on 25-Nov-26"
        val result = classifier.classify(sms, "VM-AXISBK-S")

        assertEquals(MessagePurpose.DUE_REMINDER, result.purpose)
        assertEquals(MoneyDirection.NONE, result.direction)
        assertFalse(result.isExpense)
        assertFalse(result.isFinancialTransaction)
    }

    @Test
    fun safeFallback_classifiesCardPaymentAsTransferNotExpense() {
        val sms = "Payment received of Rs 15,000.00 towards your HDFC Bank Credit Card ending 4321"
        val result = classifier.classify(sms, "VM-HDFCBK-S")

        assertEquals(MessagePurpose.CARD_PAYMENT, result.purpose)
        assertFalse("Card payment must NOT count as an expense", result.isExpense)
        assertTrue(result.isFinancialTransaction)
    }

    @Test
    fun safeFallback_classifiesOtpAsNonTransaction() {
        val sms = "123456 is your OTP for HDFC Bank login. Do not share"
        val result = classifier.classify(sms, "VM-HDFCBK-S")

        assertEquals(MessagePurpose.OTP_OR_MARKETING, result.purpose)
        assertFalse(result.isExpense)
        assertFalse(result.isFinancialTransaction)
    }
}
