package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var bankName by remember(transaction.id) { mutableStateOf(transaction.sourceBank) }
    var merchantName by remember(transaction.id) { mutableStateOf(transaction.merchant) }
    var amount by remember(transaction.id) { mutableStateOf(transaction.amount.toString()) }
    var amountError by remember(transaction.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(transaction.id) {
        bankName = transaction.sourceBank
        merchantName = transaction.merchant
        amount = transaction.amount.toString()
        amountError = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Merchant") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = null
                    },
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = amountError != null
                )
                amountError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = "Enter a valid amount"
                        return@TextButton
                    }

                    onSave(
                        transaction.copy(
                            merchant = merchantName.trim().ifBlank { transaction.merchant },
                            amount = parsedAmount,
                            sourceBank = bankName.trim().ifBlank { transaction.sourceBank }
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
