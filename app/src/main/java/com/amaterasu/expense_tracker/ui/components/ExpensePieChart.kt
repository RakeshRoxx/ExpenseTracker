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
        modifier = Modifier.size(104.dp)
    ) {
        drawArc(
            color = Color(0xFFE8EAED),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 18.dp.toPx())
        )

        drawArc(
            color = Color(0xFFEF5350),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 18.dp.toPx())
        )
    }
}
