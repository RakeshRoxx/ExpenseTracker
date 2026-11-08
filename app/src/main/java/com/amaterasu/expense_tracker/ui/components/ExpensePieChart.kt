package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ExpensePieChart(total: Double) {
    val sweepAngle = if (total != 0.0) 270f else 18f

    Canvas(
        modifier = Modifier.size(92.dp)
    ) {
        drawArc(
            color = Color.White.copy(alpha = 0.26f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx())
        )

        drawArc(
            color = Color.White,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 14.dp.toPx())
        )
    }
}
