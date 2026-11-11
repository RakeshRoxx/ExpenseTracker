package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.ui.components.CategoryIcon
import com.amaterasu.expense_tracker.ui.theme.IncomeGreen
import com.amaterasu.expense_tracker.ui.theme.PastelCoral
import com.amaterasu.expense_tracker.ui.theme.SoftSky
import com.amaterasu.expense_tracker.ui.theme.categoryPaletteFor
import utils.Utils

private val rowOuterHorizontalPadding = 16.dp
private val rowOuterVerticalPadding = 5.dp
private val rowCornerRadius = 22.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val isDebit = tx.type.trim().equals("DEBIT", ignoreCase = true)
    val isCredit = tx.type.trim().equals("CREDIT", ignoreCase = true)
    val amountColor = if (isDebit) PastelCoral else IncomeGreen
    val displayCategory = tx.category.trim().ifBlank { "OTHER" }
    val categoryPalette = categoryPaletteFor(displayCategory)
    val badgeColor = if (isDebit) categoryPalette.container else SoftSky
    val badgeContentColor = if (isDebit) categoryPalette.content else MaterialTheme.colorScheme.primary
    val transactionDetails = "${tx.sourceBank} · ${Utils.formatTimeStamp(tx.smsReceivedTimestamp)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(
                horizontal = rowOuterHorizontalPadding,
                vertical = rowOuterVerticalPadding
            ),
        shape = RoundedCornerShape(rowCornerRadius),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = badgeColor,
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                    category = displayCategory,
                    isCredit = isCredit,
                    tint = badgeContentColor,
                    modifier = Modifier.size(23.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tx.merchant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = transactionDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                when {
                    isDebit && tx.category.isNotBlank() -> {
                        Spacer(modifier = Modifier.height(7.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = categoryPalette.container
                        ) {
                            Text(
                                text = tx.category
                                    .trim()
                                    .lowercase()
                                    .replace('_', ' ')
                                    .replaceFirstChar { it.titlecase() },
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryPalette.content
                            )
                        }
                    }

                    tx.type.trim().equals("CREDIT", ignoreCase = true) -> {
                        Spacer(modifier = Modifier.height(7.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftSky
                        ) {
                            Text(
                                text = "Credit",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "${if (isDebit) "−" else "+"}₹${tx.amount}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
