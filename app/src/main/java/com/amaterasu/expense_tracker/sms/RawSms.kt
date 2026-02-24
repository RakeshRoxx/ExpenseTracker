package com.amaterasu.expense_tracker.sms

data class RawSms(
    val body: String,
    val timestamp: Long,
    val sender: String
)