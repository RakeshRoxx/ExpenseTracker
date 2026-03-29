package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.ui.components.MonthlyPieHeader
import com.amaterasu.expense_tracker.ui.components.SectionHeader
import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()
    val refreshing = viewModel.isRefreshing
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refreshFromSms() },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {

                // 🔵 Header (Pie chart + total)
                item {
                    MonthlyPieHeader(total = monthlyTotal)
                }

                // Sticky header
                stickyHeader {
                    SectionHeader(title = "Transactions")
                }

                // 💳 Transaction list
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        tx = tx,
                        onClick = { selectedTransaction = tx }
                    )
                }
            }

            selectedTransaction?.let { tx ->
                EditTransactionDialog(
                    transaction = tx,
                    onDismiss = { selectedTransaction = null },
                    onSave = { updatedTransaction ->
                        viewModel.updateTransaction(updatedTransaction)
                        selectedTransaction = null
                    }
                )
            }
        }
    }
}
