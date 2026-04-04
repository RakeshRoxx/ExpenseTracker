package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Utils

private val rowOuterHorizontalPadding = 12.dp
private val rowOuterVerticalPadding = 6.dp
private val rowCornerRadius = 18.dp
private val deleteRevealColor = Color(0xFFB71C1C)
private const val deleteFadeOutDurationMs = 500

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit
    ) {
    val amountColor = if (tx.type == "DEBIT") Color(0xFFC62828) else Color(0xFF2E7D32)
    var isVisible by remember(tx.id) { mutableStateOf(true) }
    var isDeleting by remember(tx.id) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.70f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && !isDeleting) {
                isDeleting = true
                isVisible = false
                coroutineScope.launch {
                    delay(deleteFadeOutDurationMs.toLong())
                    onDelete()
                }
                false
            } else {
                false
            }
        }
    )

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(deleteFadeOutDurationMs)) +
            shrinkVertically(animationSpec = tween(deleteFadeOutDurationMs))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = false,
            backgroundContent = {
                val swipeProgress = dismissState.progress
                val isDeleteSwipe =
                    dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd ||
                        dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                val backgroundColor =
                    when {
                        swipeProgress >= 0.3f -> deleteRevealColor
                        isDeleteSwipe -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                val contentColor =
                    if (swipeProgress >= 0.3f) Color.White
                    else if (isDeleteSwipe) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = rowOuterHorizontalPadding,
                            vertical = rowOuterVerticalPadding
                        )
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(rowCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Delete",
                                color = contentColor,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
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
                elevation = CardDefaults.cardElevation(3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tx.merchant,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tx.sourceBank,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Utils.formatTimeStamp(tx.smsReceivedTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹${tx.amount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }
                }
            }
        }
    }
}
