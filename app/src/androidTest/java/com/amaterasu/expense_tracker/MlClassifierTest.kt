package com.amaterasu.expense_tracker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.amaterasu.expense_tracker.ml.LiteRtTransactionClassifier
import com.amaterasu.expense_tracker.ml.TfidfTransactionClassifier
import domain.MessagePurpose
import domain.MoneyDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MlClassifierTest {

    @Test
    fun testLegacyTfidfClassifier() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val classifier = TfidfTransactionClassifier(context)

        val txnSms = "INR 50 debited from your account via UPI"
        val otpSms = "Your OTP is 123456"

        val txnProb = classifier.predictProbability(txnSms)
        val otpProb = classifier.predictProbability(otpSms)

        println("TXN prob=$txnProb")
        println("OTP prob=$otpProb")

        assert(txnProb > 0.6f)
        assert(otpProb < 0.4f)
    }

    @Test
    fun testLiteRtIntentClassifierOnDevice() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val classifier = LiteRtTransactionClassifier(context)

        // 1. Purchase Spend
        val purchaseSms = "INR 1,250.00 spent on your HDFC Bank Credit Card ending 4321 at ZOMATO on 10-Nov-26. Avl limit: INR 85,000.00."
        val purchaseResult = classifier.classify(purchaseSms, "VM-HDFCBK-S")
        assertEquals(MessagePurpose.PURCHASE, purchaseResult.purpose)
        assertEquals(MoneyDirection.DEBIT, purchaseResult.direction)
        assertTrue(purchaseResult.isExpense)

        // 2. Card Settlement
        val paymentSms = "Payment of Rs 15,000.00 received towards your HDFC Bank Credit Card ending 4321"
        val paymentResult = classifier.classify(paymentSms, "VM-HDFCBK-S")
        assertEquals(MessagePurpose.CARD_PAYMENT, paymentResult.purpose)
        assertFalse(paymentResult.isExpense)

        // 3. Statement Alert (must not be expense)
        val stmtSms = "Your HDFC Bank Credit Card statement of Rs. 45,000.00 is generated. Min amount due Rs. 2,500.00. Due date 20-Nov-26"
        val stmtResult = classifier.classify(stmtSms, "VM-HDFCBK-S")
        assertEquals(MessagePurpose.STATEMENT, stmtResult.purpose)
        assertEquals(MoneyDirection.NONE, stmtResult.direction)
        assertFalse(stmtResult.isExpense)

        // 4. Due Date Reminder (must not be expense)
        val dueSms = "Reminder: Payment of Rs. 12,000.00 on your Axis Bank Card XX7215 is due on 25-Nov-26"
        val dueResult = classifier.classify(dueSms, "VM-AXISBK-S")
        assertEquals(MessagePurpose.DUE_REMINDER, dueResult.purpose)
        assertFalse(dueResult.isExpense)

        // 5. OTP (must not be expense)
        val otpSms = "123456 is your OTP for HDFC Bank login. Do not share"
        val otpResult = classifier.classify(otpSms, "VM-HDFCBK-S")
        assertEquals(MessagePurpose.OTP_OR_MARKETING, otpResult.purpose)
        assertFalse(otpResult.isExpense)
    }
}
