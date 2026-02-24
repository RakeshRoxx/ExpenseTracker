package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import utils.Utils

@Composable
fun TransactionRow(tx: TransactionEntity) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = tx.merchant,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = tx.sourceBank,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "₹${tx.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = if (tx.type == "DEBIT") Color.Red else Color.Green
            )

            Text(
                text = Utils.formatTimeStamp(tx.smsReceivedTimestamp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Blue
            )
        }
    }
}
