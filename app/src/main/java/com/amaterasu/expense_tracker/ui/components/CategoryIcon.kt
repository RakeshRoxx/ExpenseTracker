package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun CategoryIcon(
    category: String,
    isCredit: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 24f
        withTransform({
            scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
        }) {
            if (isCredit) {
                drawCreditIcon(tint)
            } else {
                when (category.trim().uppercase().replace(' ', '_')) {
                    "FOOD" -> drawFoodIcon(tint)
                    "GROCERY" -> drawGroceryIcon(tint)
                    "PARTY" -> drawBeerGlassIcon(tint)
                    "TRAVEL" -> drawTravelIcon(tint)
                    "SHOPPING" -> drawShoppingIcon(tint)
                    "BILLS" -> drawBillsIcon(tint)
                    "ENTERTAINMENT" -> drawEntertainmentIcon(tint)
                    "HEALTH" -> drawHealthIcon(tint)
                    "TRANSPORT" -> drawTransportIcon(tint)
                    "TRANSFER" -> drawTransferIcon(tint)
                    else -> drawOtherIcon()
                }
            }
        }
    }
}

private const val ICON_STROKE = 1.8f

private fun DrawScope.drawFoodIcon(color: Color) {
    for (x in listOf(5f, 7f, 9f)) {
        drawIconLine(color, x, 3f, x, 9f)
    }
    drawIconLine(color, 5f, 9f, 9f, 9f)
    drawIconLine(color, 7f, 9f, 7f, 21f)
    drawCircle(color, radius = 2.5f, center = Offset(17f, 5.5f), style = Stroke(ICON_STROKE))
    drawIconLine(color, 17f, 8f, 17f, 21f)
}

private fun DrawScope.drawGroceryIcon(color: Color) {
    val handle = Path().apply {
        moveTo(8f, 8.5f)
        cubicTo(8f, 2.5f, 16f, 2.5f, 16f, 8.5f)
    }
    drawPath(handle, color, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))

    drawIconPath(
        color,
        listOf(
            Offset(5f, 8f),
            Offset(19f, 8f),
            Offset(18f, 21f),
            Offset(6f, 21f),
            Offset(5f, 8f)
        )
    )

    drawCircle(color, radius = 1.8f, center = Offset(9f, 13f))
    drawIconLine(color, 9f, 11.2f, 10f, 10.2f)

    drawIconPath(
        color,
        listOf(Offset(13f, 11f), Offset(17f, 12f), Offset(14f, 17f), Offset(13f, 11f))
    )
    drawIconLine(color, 15f, 11.5f, 15.8f, 10f)
}

private fun DrawScope.drawTravelIcon(color: Color) {
    drawIconLine(color, 3f, 8f, 9.5f, 8f)
    drawIconLine(color, 14f, 8f, 21f, 8f)
    drawIconLine(color, 3f, 8f, 7.5f, 16.5f)
    drawIconLine(color, 21f, 8f, 16.3f, 14.8f)

    drawIconLine(color, 14f, 1.5f, 14f, 8f)
    drawIconLine(color, 9.5f, 8f, 14f, 1.5f)

    val water = Path().apply {
        moveTo(2f, 20f)
        cubicTo(3f, 19.5f, 3.7f, 18f, 4.7f, 16.8f)
        lineTo(8f, 17f)
        cubicTo(9.5f, 17f, 9.7f, 18.2f, 11f, 18.2f)
        cubicTo(12.2f, 18.2f, 13.1f, 16f, 14.5f, 16f)
        cubicTo(15.8f, 16f, 16.4f, 18.2f, 17.5f, 18.2f)
        cubicTo(18.4f, 17.4f, 18.8f, 17f, 19.7f, 17.3f)
        cubicTo(20.5f, 17.7f, 21.2f, 17.2f, 22f, 17.5f)
    }
    drawPath(
        water,
        color,
        style = Stroke(width = ICON_STROKE, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawShoppingIcon(color: Color) {
    val lensStyle = Stroke(
        width = ICON_STROKE,
        cap = StrokeCap.Round,
        join = androidx.compose.ui.graphics.StrokeJoin.Round
    )

    // Left cat-eye frame: dips at the inner brow, flares up to a sharp outer wing (1.8f, 6f),
    // and tapers diagonally inward along the outer edge toward the bottom rim.
    val leftLens = Path().apply {
        moveTo(10.8f, 9.5f)
        cubicTo(8.8f, 7.8f, 5.0f, 7.4f, 1.8f, 6.0f)
        cubicTo(2.5f, 10.2f, 4.2f, 14.2f, 7.2f, 14.2f)
        cubicTo(9.6f, 14.2f, 10.8f, 12.0f, 10.8f, 9.5f)
        close()
    }

    // Right cat-eye frame (mirrored across x = 12f)
    val rightLens = Path().apply {
        moveTo(13.2f, 9.5f)
        cubicTo(15.2f, 7.8f, 19.0f, 7.4f, 22.2f, 6.0f)
        cubicTo(21.5f, 10.2f, 19.8f, 14.2f, 16.8f, 14.2f)
        cubicTo(14.4f, 14.2f, 13.2f, 12.0f, 13.2f, 9.5f)
        close()
    }

    drawPath(leftLens, color, style = lensStyle)
    drawPath(rightLens, color, style = lensStyle)

    // Arched nose bridge connecting the inner rims
    val bridge = Path().apply {
        moveTo(10.8f, 9.5f)
        cubicTo(11.4f, 8.3f, 12.6f, 8.3f, 13.2f, 9.5f)
    }
    drawPath(bridge, color, style = lensStyle)

    // Upward wing-tip flicks at the outer corners
    drawIconLine(color, 1.8f, 6.0f, 1.0f, 5.0f)
    drawIconLine(color, 22.2f, 6.0f, 23.0f, 5.0f)
}

private fun DrawScope.drawBeerGlassIcon(color: Color) {
    val glass = Path().apply {
        moveTo(5f, 7f)
        lineTo(7f, 21f)
        lineTo(16f, 21f)
        lineTo(18f, 7f)
    }
    drawPath(glass, color, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))
    drawIconLine(color, 4f, 7f, 19f, 7f)

    val handle = Path().apply {
        moveTo(18f, 10f)
        cubicTo(23f, 9f, 23f, 17f, 17.5f, 17f)
    }
    drawPath(handle, color, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))

    drawIconLine(color, 6.5f, 13f, 17f, 13f)

    drawCircle(color, radius = 1.7f, center = Offset(8f, 5.5f), style = Stroke(ICON_STROKE))
    drawCircle(color, radius = 2f, center = Offset(11.5f, 4.5f), style = Stroke(ICON_STROKE))
    drawCircle(color, radius = 1.7f, center = Offset(15f, 5.5f), style = Stroke(ICON_STROKE))
}

private fun DrawScope.drawBillsIcon(color: Color) {
    drawIconPath(
        color,
        listOf(
            Offset(6f, 3f),
            Offset(15f, 3f),
            Offset(19f, 7f),
            Offset(19f, 21f),
            Offset(5f, 21f),
            Offset(5f, 3f),
            Offset(6f, 3f)
        )
    )
    drawIconLine(color, 8f, 11f, 16f, 11f)
    drawIconLine(color, 8f, 15f, 16f, 15f)
    drawIconLine(color, 8f, 18f, 13f, 18f)
}

private fun DrawScope.drawEntertainmentIcon(color: Color) {
    drawIconPath(
        color,
        listOf(Offset(3f, 9f), Offset(21f, 9f), Offset(21f, 21f), Offset(3f, 21f)),
        close = true
    )
    drawIconPath(
        color,
        listOf(Offset(3f, 8f), Offset(5f, 3f), Offset(21f, 7f), Offset(19f, 12f))
    )
    drawIconLine(color, 9f, 4f, 7f, 9f)
    drawIconLine(color, 15f, 6f, 13f, 10f)
}

private fun DrawScope.drawHealthIcon(color: Color) {
    val heart = Path().apply {
        moveTo(12f, 21f)
        cubicTo(10f, 19f, 4f, 14f, 4f, 9f)
        cubicTo(4f, 4f, 10f, 3f, 12f, 7f)
        cubicTo(15f, 3f, 20f, 4f, 20f, 9f)
        cubicTo(20f, 14f, 14f, 19f, 12f, 21f)
    }
    drawPath(heart, color, style = Stroke(ICON_STROKE))
    drawIconLine(color, 12f, 8.5f, 12f, 15.5f)
    drawIconLine(color, 8.5f, 12f, 15.5f, 12f)
}

private fun DrawScope.drawTransportIcon(color: Color) {
    drawIconPath(
        color,
        listOf(
            Offset(3f, 15f),
            Offset(5f, 11f),
            Offset(8f, 9f),
            Offset(16f, 9f),
            Offset(19f, 12f),
            Offset(21f, 14f),
            Offset(21f, 18f),
            Offset(3f, 18f)
        ),
        close = true
    )
    drawCircle(color, 2f, Offset(7f, 18f))
    drawCircle(color, 2f, Offset(17f, 18f))
}

private fun DrawScope.drawTransferIcon(color: Color) {
    drawIconLine(color, 4f, 8f, 20f, 8f)
    drawIconLine(color, 16f, 4f, 20f, 8f)
    drawIconLine(color, 16f, 12f, 20f, 8f)
    drawIconLine(color, 20f, 16f, 4f, 16f)
    drawIconLine(color, 8f, 12f, 4f, 16f)
    drawIconLine(color, 8f, 20f, 4f, 16f)
}

private fun DrawScope.drawOtherIcon() {
    val petalColor = Color(0xFFFFC928)
    val centerColor = Color(0xFF795548)
    val seedColor = Color(0xFFD9A441)
    val stemColor = Color(0xFF4D8B46)

    val stem = Path().apply {
        moveTo(12f, 12f)
        lineTo(12f, 21f)
    }
    drawPath(stem, stemColor, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))

    val leftLeaf = Path().apply {
        moveTo(12f, 17f)
        cubicTo(10f, 14.5f, 7f, 15f, 7f, 15.5f)
        cubicTo(7f, 18.5f, 9f, 19.5f, 12f, 18f)
    }
    drawPath(leftLeaf, stemColor, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))
    drawIconLine(stemColor, 11f, 17.5f, 8.5f, 16.2f)

    val rightLeaf = Path().apply {
        moveTo(12f, 19.5f)
        cubicTo(14f, 16.5f, 17f, 16f, 17f, 16.5f)
        cubicTo(17f, 19.5f, 15f, 20.5f, 12f, 20.5f)
    }
    drawPath(rightLeaf, stemColor, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))
    drawIconLine(stemColor, 13f, 19f, 15.5f, 17.3f)

    val petals = listOf(
        Offset(12f, 2.5f),
        Offset(15.8f, 4.1f),
        Offset(18f, 8f),
        Offset(16.8f, 12.2f),
        Offset(12f, 14.5f),
        Offset(7.8f, 13.2f),
        Offset(6f, 9f),
        Offset(7.8f, 4.8f)
    )
    petals.forEach { center ->
        drawCircle(petalColor, radius = 1.7f, center = center)
    }
    drawCircle(centerColor, radius = 3.3f, center = Offset(12f, 8.5f))
    listOf(
        Offset(11f, 7.5f),
        Offset(13f, 7.8f),
        Offset(11.5f, 9.3f),
        Offset(13.2f, 9.5f)
    ).forEach { seed ->
        drawCircle(seedColor, radius = 0.45f, center = seed)
    }
}

private fun DrawScope.drawCreditIcon(color: Color) {
    drawIconLine(color, 12f, 3f, 12f, 16f)
    drawIconLine(color, 7f, 11f, 12f, 16f)
    drawIconLine(color, 17f, 11f, 12f, 16f)
    drawIconLine(color, 5f, 21f, 19f, 21f)
}

private fun DrawScope.drawIconLine(
    color: Color,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float
) {
    drawLine(
        color = color,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = ICON_STROKE,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawIconPath(
    color: Color,
    points: List<Offset>,
    close: Boolean = false
) {
    if (points.isEmpty()) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        if (close) close()
    }
    drawPath(path, color, style = Stroke(ICON_STROKE, cap = StrokeCap.Round))
}
