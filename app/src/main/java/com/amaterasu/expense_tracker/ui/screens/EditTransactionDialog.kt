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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.CategoryEntity
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.ui.components.ExpenseCategoryPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    availableCategories: List<CategoryEntity> = emptyList(),
    onCategoryCreated: (String, Int) -> Unit = { _, _ -> }
) {
    var bankName by remember(transaction.id) { mutableStateOf(transaction.sourceBank) }
    var merchantName by remember(transaction.id) { mutableStateOf(transaction.merchant) }
    var amount by remember(transaction.id) { mutableStateOf(transaction.amount.toString()) }
    var amountError by remember(transaction.id) { mutableStateOf<String?>(null) }
    var selectedCategory by remember(transaction.id) {
        mutableStateOf(transaction.category.ifBlank { "OTHER" })
    }
    LaunchedEffect(transaction.id) {
        bankName = transaction.sourceBank
        merchantName = transaction.merchant
        amount = transaction.amount.toString()
        amountError = null
        selectedCategory = transaction.category.ifBlank { "OTHER" }
    }

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
                text = "Edit transaction",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Update bank, merchant, and amount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text("Merchant") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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

            if (transaction.type == "DEBIT") {
                Spacer(modifier = Modifier.height(20.dp))
                ExpenseCategoryPicker(
                    selectedCategory = selectedCategory,
                    availableCategories = availableCategories,
                    onCategorySelected = { selectedCategory = it },
                    onCategoryAdded = { selectedCategory = it },
                    onCategoryCreated = onCategoryCreated
                )
            }

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
                        if (parsedAmount == null || parsedAmount <= 0.0) {
                            amountError = "Enter a valid amount"
                            return@Button
                        }

                        onSave(
                            transaction.copy(
                                merchant = merchantName.trim().ifBlank { transaction.merchant },
                                amount = parsedAmount,
                                category = if (transaction.type.trim().equals("DEBIT", ignoreCase = true)) {
                                    selectedCategory
                                } else {
                                    ""
                                },
                                sourceBank = bankName.trim().ifBlank { transaction.sourceBank }
                            )
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
