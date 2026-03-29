package com.amaterasu.expense_tracker.debug

import android.content.Context
import android.util.Log
import com.amaterasu.expense_tracker.ml.TfidfTransactionClassifier

object MlDebugTest {
    fun runTransactionClassifierTest(context: Context) {
        val classifier = TfidfTransactionClassifier(context)

        val samples = listOf(
            "INR 50 debited from your account via UPI at Swiggy",
            "Rs. 1200 credited to your account",
            "Your OTP is 123456",
            "Avail 10% cashback on Axis Bank cards",
            "Payment of INR 950 is due on 02-03-26"
        )

        Log.d("ML_TEST", "=== Running Transaction Classifier Tests ===")

        samples.forEach { sms ->
            val prob = classifier.predictProbability(sms)
            val isTxn = classifier.isTransaction(sms, threshold = 0.65f)

            Log.d(
                "ML_TEST",
                "SMS: '${sms.take(80)}' | prob=$prob | isTxn=$isTxn"
            )
        }

        Log.d("ML_TEST", "=== Test Completed ===")
    }
}