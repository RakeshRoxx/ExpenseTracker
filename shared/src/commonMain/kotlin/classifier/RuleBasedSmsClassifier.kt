package classifier

import domain.ClassificationResult
import domain.MessagePurpose
import domain.MoneyDirection

/**
 * Deterministic rule-based SMS intent classifier.
 *
 * Serves as the safe baseline and verification guardrail. Distinguishes message purpose
 * (PURCHASE, CARD_PAYMENT, TRANSFER, INCOME, REFUND, STATEMENT, DUE_REMINDER, OTP_OR_MARKETING, UNKNOWN)
 * from money direction (DEBIT, CREDIT, NONE, UNKNOWN).
 */
class RuleBasedSmsClassifier : SmsIntentClassifier {

    override fun classify(sms: String, sender: String): ClassificationResult {
        val normalized = sms.lowercase().trim()

        // 1. Check for OTP / Security / Verification
        if (isOtpOrAuth(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.OTP_OR_MARKETING,
                direction = MoneyDirection.NONE,
                confidence = 0.99f,
                explanation = "Matched OTP or authentication pattern"
            )
        }

        // 2. Check for Statement Generation
        if (isStatement(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.STATEMENT,
                direction = MoneyDirection.NONE,
                confidence = 0.95f,
                explanation = "Matched statement generation pattern"
            )
        }

        // 3. Check for Payment Due Reminders
        if (isDueReminder(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.DUE_REMINDER,
                direction = MoneyDirection.NONE,
                confidence = 0.95f,
                explanation = "Matched payment due reminder pattern"
            )
        }

        // 4. Check for Promotional / Marketing / Loan offers
        if (isMarketingOrOffer(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.OTP_OR_MARKETING,
                direction = MoneyDirection.NONE,
                confidence = 0.90f,
                explanation = "Matched promotional or credit offer pattern"
            )
        }

        // 5. Check for Credit Card Bill Payment / Settlement
        if (isCardBillPayment(normalized)) {
            val direction = inferDirection(normalized)
            return ClassificationResult(
                purpose = MessagePurpose.CARD_PAYMENT,
                direction = if (direction == MoneyDirection.UNKNOWN) MoneyDirection.DEBIT else direction,
                confidence = 0.92f,
                explanation = "Matched credit card bill settlement pattern"
            )
        }

        // 6. Check for Refund / Reversal
        if (isRefund(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.REFUND,
                direction = MoneyDirection.CREDIT,
                confidence = 0.90f,
                explanation = "Matched refund or reversal pattern"
            )
        }

        // 7. Check for Income / Salary
        if (isIncome(normalized)) {
            return ClassificationResult(
                purpose = MessagePurpose.INCOME,
                direction = MoneyDirection.CREDIT,
                confidence = 0.90f,
                explanation = "Matched salary or deposit pattern"
            )
        }

        // 8. Check for P2P / Account Transfer
        if (isTransfer(normalized)) {
            val direction = inferDirection(normalized)
            return ClassificationResult(
                purpose = MessagePurpose.TRANSFER,
                direction = direction,
                confidence = 0.85f,
                explanation = "Matched transfer pattern"
            )
        }

        // 9. Check for Purchase / Spend
        if (isPurchase(normalized)) {
            val direction = inferDirection(normalized)
            return ClassificationResult(
                purpose = MessagePurpose.PURCHASE,
                direction = if (direction == MoneyDirection.UNKNOWN) MoneyDirection.DEBIT else direction,
                confidence = 0.88f,
                explanation = "Matched merchant purchase or POS spend pattern"
            )
        }

        // 10. General Transaction fallback (has debit/credit keywords)
        val direction = inferDirection(normalized)
        return if (direction != MoneyDirection.UNKNOWN) {
            val purpose = if (direction == MoneyDirection.DEBIT) MessagePurpose.PURCHASE else MessagePurpose.INCOME
            ClassificationResult(
                purpose = purpose,
                direction = direction,
                confidence = 0.70f,
                explanation = "Inferred from explicit direction keywords"
            )
        } else {
            ClassificationResult(
                purpose = MessagePurpose.UNKNOWN,
                direction = MoneyDirection.UNKNOWN,
                confidence = 0.30f,
                explanation = "Unrecognized message intent"
            )
        }
    }

    private fun isOtpOrAuth(text: String): Boolean {
        val otpKeywords = listOf(
            "\\botp\\b", "\\bone time password\\b", "\\bverification code\\b",
            "\\bdo not share\\b", "\\bsecret code\\b", "\\blogin code\\b"
        )
        return otpKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isStatement(text: String): Boolean {
        val statementKeywords = listOf(
            "\\bstatement\\s+(?:is\\s+)?generated\\b",
            "\\bbill\\s+(?:is\\s+)?generated\\b",
            "\\be-?statement\\b",
            "\\bcard\\s+statement\\b",
            "\\bstatement\\s+for\\s+(?:your\\s+)?(?:account|a/c|card)\\b",
            "\\btotal\\s+amount\\s+due\\b.*\\bminimum\\b",
            "\\bstatement\\s+summary\\b"
        )
        return statementKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isDueReminder(text: String): Boolean {
        val reminderKeywords = listOf(
            "\\bpayment\\s+(?:is\\s+)?due\\b",
            "\\bpayment\\b.*?\\bis\\s+due\\b",
            "\\bis\\s+due\\s+(?:on|by)?\\b",
            "\\bdue\\s+date\\b",
            "\\bminimum\\s+amount\\s+due\\b",
            "\\bpay\\s+by\\s+\\d{1,2}[-/]\\b",
            "\\breminder\\s+to\\s+pay\\b",
            "\\breminder\\b.*?\\bdue\\b",
            "\\bbill\\s+due\\b",
            "\\bpayment\\s+reminder\\b",
            "\\bavoid\\s+late\\s+(?:fee|charges)\\b"
        )
        return reminderKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isMarketingOrOffer(text: String): Boolean {
        val marketingKeywords = listOf(
            "\\bpre-?approved\\b", "\\bloan\\s+offer\\b", "\\bapply\\s+now\\b",
            "\\bcredit\\s+limit\\s+(?:increase|enhanced)\\b",
            "\\breward\\s+points?\\b", "\\bvoucher\\b", "\\bdiscount\\s+code\\b"
        )
        return marketingKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isCardBillPayment(text: String): Boolean {
        val paymentKeywords = listOf(
            "\\bpayment\\s+.*?(?:received|successful)\\s+(?:for|towards|of).*?\\b(?:credit\\s+card|card)\\b",
            "\\bpayment\\s+of\\s+.*?(?:received|successful)\\b",
            "\\bpaid\\s+(?:for|towards)\\s+.*?card\\b",
            "\\bcredit\\s+card\\s+bill\\s+payment\\b",
            "\\bcard\\s+(?:ending|xx|no\\.?).*?payment\\s+received\\b",
            "\\breceived\\s+payment\\s+of\\s+.*?towards\\s+(?:credit\\s+)?card\\b",
            "\\bthank\\s+you\\s+for\\s+paying\\b",
            "\\bautodebit\\s+for\\s+credit\\s+card\\b"
        )
        return paymentKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isRefund(text: String): Boolean {
        val refundKeywords = listOf(
            "\\brefund\\b", "\\brefunded\\b", "\\breversal\\b", "\\breversed\\b"
        )
        return refundKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isIncome(text: String): Boolean {
        val incomeKeywords = listOf(
            "\\bsalary\\b.*\\bcredited\\b",
            "\\bcredited\\s+with\\s+salary\\b",
            "\\binterest\\s+credited\\b",
            "\\bcash\\s+deposit\\b.*\\bcredited\\b",
            "\\bdividend\\b"
        )
        return incomeKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isTransfer(text: String): Boolean {
        val transferKeywords = listOf(
            "\\btransfer(?:red)?\\s+(?:to|from)\\b",
            "\\bsent\\s+to\\b",
            "\\bupi/(?:p2p|p2a)\\b",
            "\\bimps\\b.*\\b(?:to|from)\\b",
            "\\bneft\\b.*\\b(?:to|from)\\b",
            "\\brtgs\\b"
        )
        return transferKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun isPurchase(text: String): Boolean {
        val purchaseKeywords = listOf(
            "\\bused\\s+at\\b",
            "\\bspent\\b",
            "\\bspent\\s+at\\b",
            "\\bpaid\\s+to\\b",
            "\\bpaid\\s+at\\b",
            "\\bpos\\s+txn\\b",
            "\\bpos\\s+purchase\\b",
            "\\bused\\s+for\\s+(?:inr|rs)\\b",
            "\\bused\\s+on\\b",
            "\\be-?com\\s+txn\\b",
            "\\bmerchant\\b",
            "\\bswiggy\\b", "\\bzomato\\b", "\\bamazon\\b", "\\bflipkart\\b", "\\buber\\b"
        )
        return purchaseKeywords.any { Regex(it).containsMatchIn(text) }
    }

    private fun inferDirection(text: String): MoneyDirection {
        val debitPatterns = listOf(
            "\\bdebited\\b",
            "\\bdebit\\b",
            "\\bdr\\.?\\b",
            "\\bspent\\b",
            "\\bwithdrawn\\b",
            "\\bdeducted\\b",
            "\\bsent\\b",
            "\\bpaid\\b"
        )
        val creditPatterns = listOf(
            "\\bcredited\\b",
            "\\bcredit(?:ed)?\\s+of\\b",
            "\\bcr\\.?\\b",
            "\\breceived\\b",
            "\\bdeposited\\b"
        )

        val hasDebit = debitPatterns.any { Regex(it).containsMatchIn(text) }
        val hasCredit = creditPatterns.any { Regex(it).containsMatchIn(text) }

        return when {
            hasDebit && !hasCredit -> MoneyDirection.DEBIT
            hasCredit && !hasDebit -> MoneyDirection.CREDIT
            hasDebit && hasCredit -> {
                if (Regex("""\b(?:refund|cashback|reversed?)\b""").containsMatchIn(text)) {
                    MoneyDirection.CREDIT
                } else if (Regex("""\b(?:spent|paid|withdrawn|sent)\b""").containsMatchIn(text)) {
                    MoneyDirection.DEBIT
                } else {
                    MoneyDirection.UNKNOWN
                }
            }
            else -> MoneyDirection.UNKNOWN
        }
    }
}
