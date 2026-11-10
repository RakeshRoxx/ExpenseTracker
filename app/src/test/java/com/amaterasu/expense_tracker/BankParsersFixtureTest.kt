package com.amaterasu.expense_tracker

import domain.MessagePurpose
import domain.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import parser.AxisSmsParser
import parser.DefaultSMSParser
import parser.HdfcSmsParser
import parser.IciciSmsParser

/**
 * Fixture tests verifying positive and negative examples across bank parsers.
 *
 * Guarantees:
 * - Card and account purchases remain purchases and qualify as expenses.
 * - Card bill settlements are tagged as CARD_PAYMENT and excluded from expenses.
 * - Statements, due reminders, OTPs, and promotional SMS return null (excluded entirely).
 * - Multi-amount SMS extracts the actual transaction amount, not the available balance or due total.
 */
class BankParsersFixtureTest {

    private val hdfcParser = HdfcSmsParser()
    private val axisParser = AxisSmsParser()
    private val iciciParser = IciciSmsParser()
    private val defaultParser = DefaultSMSParser()

    // =========================================================================
    // HDFC Bank Fixtures
    // =========================================================================

    @Test
    fun hdfc_cardPurchase_isParsedAsExpense() {
        val sms = "INR 1,250.00 spent on your HDFC Bank Credit Card ending 4321 at ZOMATO on 10-Nov-26. Avl limit: INR 85,000.00."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNotNull("Purchase SMS should produce a transaction", txn)
        assertEquals(1250.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals(MessagePurpose.PURCHASE, txn.purpose)
        assertTrue("Card purchase must count as an expense", txn.isExpense)
        assertEquals("HDFC", txn.sourceBank)
    }

    @Test
    fun hdfc_accountDebit_extractsCorrectAmountIgnoringBalance() {
        val sms = "Rs 450.00 debited from HDFC Bank A/C XX1234 on 10-Nov-26 to SWIGGY. Avl Bal Rs 18,500.00."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNotNull(txn)
        assertEquals(450.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals(MessagePurpose.PURCHASE, txn.purpose)
        assertTrue(txn.isExpense)
    }

    @Test
    fun hdfc_salaryIncome_isNotExpense() {
        val sms = "HDFC Bank A/C XX1234 is credited with INR 75,000.00 towards salary on 01-Nov-26. Avl Bal INR 82,300.00."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNotNull(txn)
        assertEquals(75000.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals(MessagePurpose.INCOME, txn.purpose)
        assertFalse("Salary income must not count as an expense", txn.isExpense)
    }

    @Test
    fun hdfc_statementGenerated_isExcludedEntirely() {
        val sms = "Your HDFC Bank Credit Card statement of Rs. 42,500.00 is generated. Min amount due is Rs. 2,100.00. Due date: 20-Nov-26."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNull("Credit card statement alert must be excluded from transactions", txn)
    }

    @Test
    fun hdfc_dueReminder_isExcludedEntirely() {
        val sms = "Reminder: Payment of Rs. 42,500.00 for your HDFC Bank Credit Card ending 4321 is due on 20-Nov-26. Pay now to avoid late fee."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNull("Payment due reminder must be excluded from transactions", txn)
    }

    @Test
    fun hdfc_cardBillPaymentSettlement_isTransferNotExpense() {
        val sms = "Payment of Rs 15,000.00 received towards your HDFC Bank Credit Card ending 4321 on 15-Nov-26. Thank you for paying."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNotNull("Bill settlement may be tracked as a transfer", txn)
        assertEquals(15000.0, txn!!.amount, 0.001)
        assertEquals(MessagePurpose.CARD_PAYMENT, txn.purpose)
        assertFalse("Credit card bill settlement must NOT count as a purchase expense", txn.isExpense)
    }

    @Test
    fun hdfc_otp_isExcludedEntirely() {
        val sms = "458921 is your OTP for HDFC Bank NetBanking transaction of INR 1,250.00. Do not share with anyone."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNull("OTP message must be excluded from transactions", txn)
    }

    @Test
    fun hdfc_loanMarketingOffer_isExcludedEntirely() {
        val sms = "Dear Customer, you have a pre-approved personal loan of Rs 5,00,000 from HDFC Bank at low interest. Apply now."
        val txn = hdfcParser.parse(sms, 1710000000000L, "VM-HDFCBK-S")

        assertNull("Marketing/loan offer must be excluded from transactions", txn)
    }

    // =========================================================================
    // Axis Bank Fixtures
    // =========================================================================

    @Test
    fun axis_cardSpend_isParsedAsExpense() {
        val sms = "Axis Bank Credit Card no. XX7215 spent for INR 899.00 at AMAZON on 10-Nov-26. Avl limit: INR 45,000.00."
        val txn = axisParser.parse(sms, 1710000000000L, "VM-AXISBK-S")

        assertNotNull(txn)
        assertEquals(899.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals(MessagePurpose.PURCHASE, txn.purpose)
        assertTrue(txn.isExpense)
        assertEquals("AXIS", txn.sourceBank)
    }

    @Test
    fun axis_accountDebit_isParsedAsExpense() {
        val sms = "INR 250.00 debited from Axis Bank A/C XX9876 on 10-Nov-26. UPI Ref 623412. Avl Bal INR 12,300.00."
        val txn = axisParser.parse(sms, 1710000000000L, "VM-AXISBK-S")

        assertNotNull(txn)
        assertEquals(250.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertTrue(txn.isExpense)
    }

    @Test
    fun axis_statementGenerated_isExcludedEntirely() {
        val sms = "Your Axis Bank Credit Card XX7215 statement has been generated. Total amount due: INR 12,400.00, due date 25-Nov-26."
        val txn = axisParser.parse(sms, 1710000000000L, "VM-AXISBK-S")

        assertNull("Statement alert must not create transaction", txn)
    }

    @Test
    fun axis_dueReminder_isExcludedEntirely() {
        val sms = "Payment of INR 12,400.00 for Axis Bank Credit Card no. XX7215 is due on 25-Nov-26 with minimum amount due of INR 1,200.00."
        val txn = axisParser.parse(sms, 1710000000000L, "VM-AXISBK-S")

        assertNull("Payment due reminder must not create transaction", txn)
    }

    @Test
    fun axis_billPaymentReceived_isTransferNotExpense() {
        val sms = "Payment received of INR 12,400.00 towards your Axis Bank Credit Card XX7215 on 20-Nov-26."
        val txn = axisParser.parse(sms, 1710000000000L, "VM-AXISBK-S")

        assertNotNull(txn)
        assertEquals(12400.0, txn!!.amount, 0.001)
        assertEquals(MessagePurpose.CARD_PAYMENT, txn.purpose)
        assertFalse("Bill payment receipt is not a purchase expense", txn.isExpense)
    }

    // =========================================================================
    // ICICI Bank Fixtures
    // =========================================================================

    @Test
    fun icici_cardDebit_isParsedAsExpense() {
        val sms = "ICICI Bank Credit Card XX1006 debited for INR 1,500.00 on 10-Nov-26. Info: FLIPKART. Avl Limit: INR 65,000.00."
        val txn = iciciParser.parse(sms, 1710000000000L, "VM-ICICIB-S")

        assertNotNull(txn)
        assertEquals(1500.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals(MessagePurpose.PURCHASE, txn.purpose)
        assertTrue(txn.isExpense)
        assertEquals("ICICI", txn.sourceBank)
    }

    @Test
    fun icici_refundCredited_isRefundNotExpense() {
        val sms = "Refund of INR 650.00 credited to ICICI Bank A/C XX5432 on 10-Nov-26 from SWIGGY. Avl Bal INR 9,200.00."
        val txn = iciciParser.parse(sms, 1710000000000L, "VM-ICICIB-S")

        assertNotNull(txn)
        assertEquals(650.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals(MessagePurpose.REFUND, txn.purpose)
        assertFalse(txn.isExpense)
    }

    @Test
    fun icici_statementGenerated_isExcludedEntirely() {
        val sms = "ICICI Bank Credit Card statement for XX1006 is generated. Total due: INR 28,000.00, min due: INR 1,500.00. Due date: 18-Nov-26."
        val txn = iciciParser.parse(sms, 1710000000000L, "VM-ICICIB-S")

        assertNull("ICICI statement must return null", txn)
    }

    @Test
    fun icici_dueReminder_isExcludedEntirely() {
        val sms = "Payment is due on 18-Nov-26 for ICICI Bank Credit Card XX1006. Minimum amount due: INR 1,500.00. Pay now."
        val txn = iciciParser.parse(sms, 1710000000000L, "VM-ICICIB-S")

        assertNull("ICICI due reminder must return null", txn)
    }

    @Test
    fun icici_cardBillPayment_isTransferNotExpense() {
        val sms = "Thank you for paying INR 28,000.00 towards your ICICI Bank Credit Card XX1006 on 17-Nov-26."
        val txn = iciciParser.parse(sms, 1710000000000L, "VM-ICICIB-S")

        assertNotNull(txn)
        assertEquals(28000.0, txn!!.amount, 0.001)
        assertEquals(MessagePurpose.CARD_PAYMENT, txn.purpose)
        assertFalse(txn.isExpense)
    }

    // =========================================================================
    // Default / Generic Bank Fixtures
    // =========================================================================

    @Test
    fun defaultParser_excludesStatementAlerts() {
        val sms = "Your credit card bill for BOBCARD XXXX-XXXX has been generated. Total amount: INR 5,400.00. Due date: 28-Nov-26."
        val txn = defaultParser.parse(sms, 1710000000000L, "VM-BOBCRD-S")

        assertNull(txn)
    }

    @Test
    fun defaultParser_parsesDebitAndSkipsBalance() {
        val sms = "Dear Customer, Your A/C XXXXX995321 has a debit by NACH of Rs 2,000.00 on 26/10/24. Avl Bal Rs 12,788.43. Download YONO - SBI"
        val txn = defaultParser.parse(sms, 1710000000000L, "VK-SBIINB-S")

        assertNotNull(txn)
        assertEquals(2000.0, txn!!.amount, 0.001)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertTrue(txn.isExpense)
    }
}
