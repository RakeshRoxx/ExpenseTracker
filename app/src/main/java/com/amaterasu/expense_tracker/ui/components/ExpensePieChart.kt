package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ExpensePieChart(
    categoryExpenses: List<CategoryExpenseSummary>
) {
    val totalExpenses = categoryExpenses.sumOf { it.amount }

    Canvas(
        modifier = Modifier.size(92.dp)
    ) {
        drawArc(
            color = Color.White.copy(alpha = 0.26f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Butt)
        )

        if (totalExpenses > 0.0) {
            var startAngle = -90f
            val gapAngle = if (categoryExpenses.size > 1) 1.5f else 0f

            categoryExpenses.forEach { expense ->
                val sweepAngle = (expense.amount / totalExpenses * 360f).toFloat()
                val visibleSweep = (sweepAngle - gapAngle).coerceAtLeast(0f)
                if (visibleSweep > 0f) {
                    drawArc(
                        color = expense.color,
                        startAngle = startAngle + gapAngle / 2f,
                        sweepAngle = visibleSweep,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Butt)
                    )
                }
                startAngle += sweepAngle
            }
        }
    }
}
