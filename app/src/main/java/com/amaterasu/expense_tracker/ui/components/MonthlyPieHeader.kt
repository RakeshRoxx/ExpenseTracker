package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class CategoryExpenseSummary(
    val category: String,
    val amount: Double,
    val color: Color
)

@Composable
fun MonthlyPieHeader(
    categoryExpenses: List<CategoryExpenseSummary>
) {
    val totalExpenses = categoryExpenses.sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7953D8),
                            Color(0xFFB45BD7),
                            Color(0xFFFF856C)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "This month",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.84f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹ %.2f".format(totalExpenses),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "Total expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.86f)
                    )
                }

                ExpensePieChart(categoryExpenses = categoryExpenses)
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (categoryExpenses.isEmpty()) {
                Text(
                    text = "No expenses this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f)
                )
            } else {
                Text(
                    text = "Expense breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                categoryExpenses.forEach { expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(expense.color, CircleShape)
                        )
                        Text(
                            text = categoryLabel(expense.category),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 9.dp, end = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                        Text(
                            text = "₹ %.2f".format(expense.amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "${(expense.amount / totalExpenses * 100).roundToInt()}%",
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(width = 38.dp, height = 20.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.86f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun categoryLabel(category: String): String =
    category
        .trim()
        .lowercase()
        .replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { character -> character.titlecase() }
        }
