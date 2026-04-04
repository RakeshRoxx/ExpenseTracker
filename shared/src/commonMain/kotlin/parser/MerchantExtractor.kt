package parser

import domain.TransactionType

object MerchantExtractor {

    private val stopPhrases = listOf(
        " not you",
        " sms ",
        " call ",
        " avl ",
        " avail ",
        " available ",
        " bal:",
        " balance:",
        " ref:",
        " ref no",
        " upi ref",
        " utr",
        " on ",
        " on\n",
        " using ",
        " by ",
        " via ",
        " from your",
        " to your",
        " if not",
        " if you",
        " to dispute",
        " updated balance",
        " avl bal",
        " available balance"
    )

    private val invalidTokens = setOf(
        "inr", "rs", "upi", "p2m", "p2a", "imps", "neft", "rtgs", "nach",
        "debit", "debited", "credit", "credited", "dr", "cr", "txn", "id",
        "account", "a/c", "bank", "card", "credit card", "debit card", "statement", "minimum amount due",
        "pos", "merchant", "transfer", "payment", "withdrawal", "deposit"
    )

    private val popularMerchantAliases = linkedMapOf(
        "amazon pay" to "Amazon Pay",
        "amazon" to "Amazon",
        "swiggy" to "Swiggy",
        "zomato" to "Zomato",
        "bigbasket" to "BigBasket",
        "flipkart" to "Flipkart",
        "dmart" to "DMart",
        "zepto" to "Zepto",
        "blinkit" to "Blinkit",
        "cred club" to "CRED Club",
        "cred" to "CRED",
        "google pay" to "Google Pay",
        "gpay" to "Google Pay",
        "phonepe" to "PhonePe",
        "paytm" to "Paytm",
        "uber" to "Uber",
        "ola" to "Ola",
        "myntra" to "Myntra",
        "mcdonald" to "McDonald's",
        "posterized" to "Posterized",
        "fatima" to "Fatima"
    )

    private val contextualPatterns = listOf(
        Regex("""\b(?:paid|sent)\s+(?:to|for)\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:received|credited)\s+(?:from|by)\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bused\s+at\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bspent(?:\s+inr\s+[0-9,]+(?:\.[0-9]{1,2})?)?\s+[A-Za-z0-9 ]*?at\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bfor\s+UPI-[A-Za-z0-9]+-([A-Za-z][A-Za-z0-9&._' -]{1,})""", RegexOption.IGNORE_CASE),
        Regex("""\bUPI/[Pp]2[AM]/[^/\s]+/([A-Za-z][A-Za-z0-9&._' -]{1,})"""),
        Regex("""\bto\s+([A-Za-z][A-Za-z0-9&._' -]{1,})\s*\(UPI\s+Ref""", RegexOption.IGNORE_CASE),
        Regex("""\bfrom\s+([A-Za-z][A-Za-z0-9&._' -]{1,})\s*\(UPI\s+Ref""", RegexOption.IGNORE_CASE)
    )

    fun extract(text: String, transactionType: TransactionType? = null): String? {
        contextualPatterns.forEach { pattern ->
            val candidate = pattern.find(text)?.groupValues?.getOrNull(1)
            cleanCandidate(candidate)?.let { return normalizeAlias(it) }
        }

        extractPopularMerchant(text)?.let { return it }

        return null
    }

    private fun extractPopularMerchant(text: String): String? {
        val normalizedText = text.lowercase()
        for ((needle, merchant) in popularMerchantAliases) {
            if (needle in normalizedText) {
                return merchant
            }
        }
        return null
    }

    private fun cleanCandidate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null

        var candidate = raw
            .replace(Regex("""[\r\n]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '.', ',', ':', ';', '-', '/', '(', ')')

        cleanupPatterns.forEach { pattern ->
            candidate = candidate.replace(pattern, "")
        }

        stopPhrases.forEach { stop ->
            val lowered = candidate.lowercase()
            val index = lowered.indexOf(stop)
            if (index > 0) {
                candidate = candidate.substring(0, index)
            }
        }

        candidate = candidate
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '.', ',', ':', ';', '-', '/', '(', ')')

        if (candidate.length < 2) return null
        if (candidate.none { it.isLetter() }) return null
        if (maskedOrNumericPattern.matches(candidate)) return null
        if (dateLikePattern.containsMatchIn(candidate)) return null
        if (containsReferenceNoise(candidate)) return null
        if (looksLikeSuspiciousHandle(candidate)) return null

        val normalized = candidate.lowercase()
        if (normalized in invalidTokens) return null
        if (invalidTokens.any { token ->
                normalized == token ||
                    normalized.startsWith("$token ") ||
                    normalized.endsWith(" $token")
            }
        ) return null

        return candidate
    }

    private fun normalizeAlias(candidate: String): String {
        val lowered = candidate.lowercase()
        for ((needle, merchant) in popularMerchantAliases) {
            if (needle in lowered) return merchant
        }
        return candidate
    }

    private val cleanupPatterns = listOf(
        Regex("""\s*\(?(?:UPI\s+Ref|Ref(?:\s+No)?|UTR|Txn\s+Id|Transaction\s+Id)\b.*$""", RegexOption.IGNORE_CASE),
        Regex("""\s+\bfor\s+(?:INR|Rs[.]?|₹)\s+[\d,]+(?:\.\d{1,2})?.*$""", RegexOption.IGNORE_CASE)
    )

    private val maskedOrNumericPattern = Regex("""^[xX*\d\s@._/-]+$""")
    private val dateLikePattern = Regex("""\b\d{1,2}[-/](?:\d{1,2}|[A-Za-z]{3})[-/]\d{2,4}\b""")
    private val referenceNoisePattern = Regex(
        """\b(?:ref|utr|txn|transaction|account|a/c|card|bank|balance|avl|available|if\s+not|call)\b""",
        RegexOption.IGNORE_CASE
    )

    private fun containsReferenceNoise(candidate: String): Boolean {
        return referenceNoisePattern.containsMatchIn(candidate)
    }

    private fun looksLikeSuspiciousHandle(candidate: String): Boolean {
        if ('@' in candidate) return true

        val compact = candidate.replace(" ", "")
        val digitCount = compact.count { it.isDigit() }
        val letterCount = compact.count { it.isLetter() }
        val onlyBasicHandleChars = compact.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }

        return onlyBasicHandleChars &&
            digitCount >= 2 &&
            letterCount >= 2 &&
            !compact.contains('&') &&
            !compact.contains('\'')
    }
}
