package parser

abstract class BankSmsParser : SmsParser {

    protected fun extractAmount(text: String): Double? {
        val regex = Regex(
            "(?:INR|Rs\\.?|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
            RegexOption.IGNORE_CASE
        )

        val match = regex.find(text)
        val raw = match?.groupValues?.get(1)

        return raw
            ?.replace(",", "")
            ?.toDoubleOrNull()
    }
}