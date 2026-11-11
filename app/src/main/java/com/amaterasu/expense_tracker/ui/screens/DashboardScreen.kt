package com.amaterasu.expense_tracker.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.sms.SmsPermission
import com.amaterasu.expense_tracker.ui.components.CategoryExpenseSummary
import com.amaterasu.expense_tracker.ui.components.MonthlyPieHeader
import com.amaterasu.expense_tracker.ui.components.SectionHeader
import com.amaterasu.expense_tracker.ui.theme.categoryPaletteFor
import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel
import java.time.Instant
import java.time.LocalDate
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
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val netMonthlyExpense by viewModel.monthlyTotal.collectAsState()
    val refreshing = viewModel.isRefreshing
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var smsPreviewTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val consentPreferences = remember(context) {
        context.getSharedPreferences("sms_access_consent", Context.MODE_PRIVATE)
    }
    var hasSmsPermission by remember(context) {
        mutableStateOf(SmsPermission.hasPermission(context))
    }
    var hasAcceptedSmsDisclosure by remember(context) {
        mutableStateOf(consentPreferences.getBoolean("accepted", false))
    }
    var showSmsDisclosure by remember { mutableStateOf(false) }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSmsPermission = granted
        if (granted) {
            viewModel.refreshFromSms()
        }
    }
    val requestSmsImport = {
        val permissionGranted = SmsPermission.hasPermission(context)
        hasSmsPermission = permissionGranted
        if (permissionGranted && hasAcceptedSmsDisclosure) {
            viewModel.refreshFromSms()
        } else {
            showSmsDisclosure = true
        }
    }
    val monthlyExpenseBreakdown = remember(transactions, expenseCategories) {
        val zoneId = ZoneId.systemDefault()
        val monthStart = LocalDate.now().withDayOfMonth(1)
        val startMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val nextMonthMillis = monthStart.plusMonths(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        transactions
            .asSequence()
            .filter { transaction ->
                transaction.type.trim().equals("DEBIT", ignoreCase = true) &&
                    !transaction.category.trim().equals("TRANSFER", ignoreCase = true) &&
                    transaction.smsReceivedTimestamp >= startMillis &&
                    transaction.smsReceivedTimestamp < nextMonthMillis
            }
            .groupBy { transaction ->
                transaction.category.trim().ifBlank { "OTHER" }.lowercase()
            }
            .values
            .mapNotNull { categoryTransactions ->
                val category = categoryTransactions.first().category.trim().ifBlank { "OTHER" }
                val amount = categoryTransactions.sumOf { it.amount }
                if (amount <= 0.0) return@mapNotNull null

                val savedColorIndex = expenseCategories
                    .firstOrNull { it.name.equals(category, ignoreCase = true) }
                    ?.colorIndex
                val palette = categoryPaletteFor(category, savedColorIndex)
                CategoryExpenseSummary(
                    category = category,
                    amount = amount,
                    color = palette.content
                )
            }
            .sortedByDescending { it.amount }
    }
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
        if (!consentPreferences.getBoolean("seen", false)) {
            showSmsDisclosure = true
        }
        if (hasSmsPermission && hasAcceptedSmsDisclosure) {
            viewModel.runLaunchTestImportIfEnabled()
        }
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { requestSmsImport() },
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hi, Buddy!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Here’s your money at a glance",
                                modifier = Modifier.padding(top = 3.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                uriHandler.openUri(RUPEEENSE_PRIVACY_POLICY_URL)
                            }
                        ) {
                            Text("Privacy")
                        }
                    }
                }

                if (!hasSmsPermission || !hasAcceptedSmsDisclosure) {
                    item {
                        SmsImportPermissionCard(
                            onClick = { showSmsDisclosure = true }
                        )
                    }
                }

                item {
                    MonthlyPieHeader(
                        categoryExpenses = monthlyExpenseBreakdown,
                        netMonthlyExpense = netMonthlyExpense
                    )
                }

                if (refreshing) {
                    item {
                        RefreshingHint()
                    }
                }

                if (groupedTransactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Transactions",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
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
                    }
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

            if (showSmsDisclosure) {
                SmsAccessDisclosureDialog(
                    onAgree = {
                        consentPreferences.edit()
                            .putBoolean("seen", true)
                            .putBoolean("accepted", true)
                            .apply()
                        hasAcceptedSmsDisclosure = true
                        showSmsDisclosure = false

                        if (SmsPermission.hasPermission(context)) {
                            hasSmsPermission = true
                            viewModel.refreshFromSms()
                        } else {
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    },
                    onNotNow = {
                        consentPreferences.edit()
                            .putBoolean("seen", true)
                            .putBoolean("accepted", false)
                            .apply()
                        hasAcceptedSmsDisclosure = false
                        showSmsDisclosure = false
                    },
                    onOpenPrivacyPolicy = {
                        uriHandler.openUri(RUPEEENSE_PRIVACY_POLICY_URL)
                    }
                )
            }

            FloatingActionButton(
                onClick = { showAddTransactionSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .navigationBarsPadding(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (showAddTransactionSheet) {
                AddTransactionDialog(
                    onDismiss = { showAddTransactionSheet = false },
                    availableCategories = expenseCategories,
                    onCategoryCreated = { name, colorIndex ->
                        viewModel.addExpenseCategory(name, colorIndex)
                    },
                    onSave = { amount, type, merchant, sourceBank, category ->
                        viewModel.addManualTransaction(
                            amount = amount,
                            type = type,
                            merchant = merchant,
                            sourceBank = sourceBank,
                            category = category
                        )
                        showAddTransactionSheet = false
                    }
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
