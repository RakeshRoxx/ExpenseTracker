package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: String, merchant: String, sourceBank: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("DEBIT") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var merchantError by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add transaction",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Date will be set to the current system time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = null
                },
                label = { Text("Amount") },
                singleLine = true,
                isError = amountError != null,
                modifier = Modifier.fillMaxWidth()
            )
            amountError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = transactionType == "DEBIT",
                    onClick = { transactionType = "DEBIT" },
                    label = { Text("Debit") }
                )
                FilterChip(
                    selected = transactionType == "CREDIT",
                    onClick = { transactionType = "CREDIT" },
                    label = { Text("Credit") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = merchantName,
                onValueChange = {
                    merchantName = it
                    merchantError = null
                },
                label = { Text("Merchant") },
                singleLine = true,
                isError = merchantError != null,
                modifier = Modifier.fillMaxWidth()
            )
            merchantError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        val trimmedMerchant = merchantName.trim()
                        val trimmedBank = bankName.trim()

                        var hasError = false
                        if (parsedAmount == null || parsedAmount <= 0.0) {
                            amountError = "Enter a valid amount"
                            hasError = true
                        }
                        if (trimmedMerchant.isBlank()) {
                            merchantError = "Enter a merchant"
                            hasError = true
                        }
                        if (hasError) return@Button

                        onSave(
                            parsedAmount!!,
                            transactionType,
                            trimmedMerchant,
                            trimmedBank
                        )
                    }
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
