package domain

import kotlinx.serialization.Serializable

@Serializable
enum class MessagePurpose {
    /**
     * Actual point-of-sale, e-commerce, merchant spend, or bill payment for utilities.
     * Counts as an expense when direction is DEBIT.
     */
    PURCHASE,

    /**
     * Credit card bill settlement or loan repayment.
     * Represents a transfer/settlement of already-recorded debt; NOT a new purchase expense.
     */
    CARD_PAYMENT,

    /**
     * Person-to-person transfer (UPI P2P, NEFT, IMPS) or self-transfer between own accounts.
     */
    TRANSFER,

    /**
     * Inflow of funds: salary, deposits, incoming transfers, interest earned.
     */
    INCOME,

    /**
     * Return of funds previously spent: merchant refund, reversal of failed debit.
     */
    REFUND,

    /**
     * Monthly or periodic statement generated notification.
     * Contains billing summary, NOT an individual transactional event.
     */
    STATEMENT,

    /**
     * Upcoming payment due date reminder or minimum due notification.
     * Informational reminder, NOT an actual fund movement.
     */
    DUE_REMINDER,

    /**
     * One-time password, two-factor auth, service notification, loan offer, or marketing promo.
     */
    OTP_OR_MARKETING,

    /**
     * Ambiguous, unrecognized, or low-confidence message.
     * Must be excluded from expense totals or routed for explicit user review.
     */
    UNKNOWN
}
