package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.ui.components.MonthlyPieHeader
import com.amaterasu.expense_tracker.ui.components.SectionHeader
import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthHeaderFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

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
    var smsPreviewTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { transaction ->
            Instant.ofEpochMilli(transaction.smsReceivedTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .withDayOfMonth(1)
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.runLaunchTestImportIfEnabled()
    }

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

                if (refreshing) {
                    item {
                        RefreshingHint()
                    }
                }

                when {
                    groupedTransactions.isEmpty() && refreshing -> {
                        items(4) { index ->
                            TransactionRowSkeleton(index = index)
                        }
                    }

                    groupedTransactions.isEmpty() -> {
                        item {
                            EmptyTransactionsState()
                        }
                    }

                    else -> {
                        groupedTransactions.forEach { (month, monthTransactions) ->
                            stickyHeader(key = "month-${month}") {
                                SectionHeader(title = month.format(monthHeaderFormatter))
                            }

                            items(monthTransactions, key = { it.id }) { tx ->
                                TransactionRow(
                                    tx = tx,
                                    onClick = { selectedTransaction = tx },
                                    onLongPress = { smsPreviewTransaction = tx }
                                )
                            }
                        }
                    }
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

            smsPreviewTransaction?.let { tx ->
                TransactionSmsDialog(
                    transaction = tx,
                    onDismiss = { smsPreviewTransaction = null }
                )
            }
        }
    }
}

@Composable
private fun RefreshingHint() {
    Text(
        text = "Refreshing transactions...",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EmptyTransactionsState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No transactions for this month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Pull down to refresh and import the latest SMS transactions.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransactionRowSkeleton(index: Int) {
    val merchantWidth = if (index % 2 == 0) 0.52f else 0.64f
    val bankWidth = if (index % 2 == 0) 0.34f else 0.28f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(merchantWidth)
                    .height(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(bankWidth)
                    .height(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.24f)
                    .height(14.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}
