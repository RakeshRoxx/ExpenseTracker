package domain

import kotlinx.serialization.Serializable

@Serializable
enum class MoneyDirection {
    DEBIT,
    CREDIT,
    NONE,
    UNKNOWN
}
