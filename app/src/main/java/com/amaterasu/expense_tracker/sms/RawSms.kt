package com.amaterasu.expense_tracker.sms

data class RawSms(
    val id: Long,
    val body: String,
    val timestamp: Long,
    val sender: String
)
