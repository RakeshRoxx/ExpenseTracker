package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var smsPreviewTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onClick = {
                        smsPreviewTransaction = null
                        editingTransaction = tx
                    },
                    onLongPress = {
                        editingTransaction = null
                        smsPreviewTransaction = tx
                    }
                )
            }
        }

        editingTransaction?.let { tx ->
            EditTransactionDialog(
                transaction = tx,
                availableCategories = expenseCategories,
                onCategoryCreated = { name, colorIndex ->
                    viewModel.addExpenseCategory(name, colorIndex)
                },
                onDismiss = { editingTransaction = null },
                onSave = { updatedTransaction ->
                    viewModel.updateTransaction(updatedTransaction)
                    editingTransaction = null
                },
                onDelete = {
                    viewModel.deleteTransaction(tx)
                    editingTransaction = null
                }
            )
        }

        smsPreviewTransaction?.let { tx ->
            TransactionSmsDialog(
                transaction = tx,
                onDismiss = { smsPreviewTransaction = null },
                loadOriginalSms = viewModel::loadOriginalSmsBody
            )
        }
    }
}
