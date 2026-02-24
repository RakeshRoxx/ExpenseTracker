package com.amaterasu.expense_tracker.data.mapper

import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import domain.Transaction

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    merchant = merchant,
    smsReceivedTimestamp = smsReceivedTimestamp,
    parsingTimestamp = parsingTimestamp,
    category = category.name,
    source = source.name,
    accountHint = accountHint,
    sourceBank = sourceBank
)