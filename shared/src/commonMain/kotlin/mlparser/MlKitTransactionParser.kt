package mlparser

import com.google.mlkit.nl.entityextraction.Entity;
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import domain.MLParsedTransaction
import kotlinx.coroutines.tasks.await


class MlKitTransactionParser {

    private val extractor = EntityExtraction.getClient(
        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
    )

    suspend fun parse(sms: String): MLParsedTransaction {
        val params = EntityExtractionParams.Builder(sms).build()
        val annotations = extractor.annotate(params).await()

        var amount: Double? = null
        var timestamp: Long? = null

        for (annotation in annotations) {
            for (entity in annotation.entities) {
                when (entity.type) {
                    Entity.TYPE_MONEY -> {
                        val rawAmount = annotation.annotatedText
                        amount = rawAmount
                            .replace(",", "")
                            .replace(Regex("[^0-9.]"), "")
                            .toDoubleOrNull()
                    }
                    Entity.TYPE_DATE_TIME -> {
                        timestamp = entity.asDateTimeEntity()?.timestampMillis
                    }
                }
            }
        }

        val merchant = extractMerchantHeuristically(sms) ?: extractMerchantFallback(sms)

        val isDebit = sms.contains("debit", true) ||
                sms.contains("spent", true) ||
                sms.contains("paid", true)

        return MLParsedTransaction(
            amount = amount,
            merchant = merchant,
            timestamp = timestamp,
            isDebit = isDebit
        )
    }

    private fun extractMerchantHeuristically(sms: String): String? {
        val patterns = listOf(
            Regex("at\\s+([A-Z0-9 _.-]+)", RegexOption.IGNORE_CASE),
            Regex("to\\s+([A-Z0-9 _.-]+)", RegexOption.IGNORE_CASE),
            Regex("@([A-Z0-9 _.-]+)"),
            Regex("via\\s+([A-Z0-9 _.-]+)", RegexOption.IGNORE_CASE)
        )
        return patterns.firstNotNullOfOrNull { it.find(sms)?.groupValues?.get(1)?.trim() }
    }

    private fun extractMerchantFallback(sms: String): String? {
        val blacklist = setOf("INR", "RS", "UPI", "IMPS", "NEFT", "DEBIT", "CREDIT", "BAL", "A/C")
        return Regex("\\b[A-Z][A-Z0-9 &._-]{3,}\\b")
            .findAll(sms)
            .map { it.value }
            .firstOrNull { c -> blacklist.none { b -> c.contains(b, true) } }
    }
}
