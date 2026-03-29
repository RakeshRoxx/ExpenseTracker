package com.amaterasu.expense_tracker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.amaterasu.expense_tracker.ml.TfidfTransactionClassifier
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MlClassifierTest {
    @Test
    fun testTransactionClassifier() {
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
}