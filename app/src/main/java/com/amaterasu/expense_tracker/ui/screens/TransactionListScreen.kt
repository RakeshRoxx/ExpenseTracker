package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(transactions) { tx ->
            TransactionRow(tx)
        }
    }
}
