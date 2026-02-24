package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity

@Composable
fun MonthlyPieHeader(
    transactions: List<TransactionEntity>
) {
    val total = transactions
        .filter { it.type == "DEBIT" }
        .sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExpensePieChart(
            total = total
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This Month",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "₹ %.2f".format(total),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
