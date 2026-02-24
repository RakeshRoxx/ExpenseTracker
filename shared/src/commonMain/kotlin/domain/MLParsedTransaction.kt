package domain

data class MLParsedTransaction (
    val amount: Double?,
    val merchant: String?,
    val timestamp: Long?,
    val isDebit: Boolean?
    )