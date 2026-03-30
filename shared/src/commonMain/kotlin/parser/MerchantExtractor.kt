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
        " by ",
        " via ",
        " from your",
        " to your",
        " if not",
        " if you",
        " to dispute",
        " updated balance"
    )

    private val invalidTokens = setOf(
        "inr", "rs", "upi", "p2m", "p2a", "imps", "neft", "rtgs", "nach",
        "debit", "debited", "credit", "credited", "dr", "cr", "txn", "id",
        "account", "a/c", "bank", "card", "credit card", "statement", "minimum amount due"
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
        "cred club" to "CRED Club",
        "cred" to "CRED",
        "mcdonald" to "McDonald's",
        "posterized" to "Posterized",
        "fatima" to "Fatima"
    )

    private val contextualPatterns = listOf(
        Regex("""\b(?:paid|sent)\s+(?:to|for)\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:received|credited)\s+(?:from|by)\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bused\s+at\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bspent(?:\s+inr\s+[0-9,]+(?:\.[0-9]{1,2})?)?\s+[A-Za-z0-9 ]*?at\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
        Regex("""\bat\s+([A-Za-z0-9&._'()\/ -]{2,})""", RegexOption.IGNORE_CASE),
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

        val directionalFallback = when (transactionType) {
            TransactionType.CREDIT -> Regex("""\bfrom\s+([A-Za-z][A-Za-z0-9&._' -]{1,})""", RegexOption.IGNORE_CASE)
            TransactionType.DEBIT -> Regex("""\bto\s+([A-Za-z][A-Za-z0-9&._' -]{1,})""", RegexOption.IGNORE_CASE)
            null -> null
        }
        directionalFallback
            ?.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::cleanCandidate)
            ?.let(::normalizeAlias)
            ?.let { return it }

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
        if (candidate.all { it.isDigit() || it == ' ' }) return null

        val normalized = candidate.lowercase()
        if (normalized in invalidTokens) return null
        if (invalidTokens.any { normalized.contains(it) && normalized.length <= it.length + 4 }) return null

        return candidate
    }

    private fun normalizeAlias(candidate: String): String {
        val lowered = candidate.lowercase()
        for ((needle, merchant) in popularMerchantAliases) {
            if (needle in lowered) return merchant
        }
        return candidate
    }
}
