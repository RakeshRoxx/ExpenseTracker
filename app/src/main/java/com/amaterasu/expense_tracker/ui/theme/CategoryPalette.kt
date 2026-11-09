package com.amaterasu.expense_tracker.ui.theme

import androidx.compose.ui.graphics.Color

data class CategoryPalette(
    val index: Int,
    val container: Color,
    val content: Color
)

private val categoryPalettes = listOf(
    CategoryPalette(0, Color(0xFFE6F5EA), Color(0xFF256844)),
    CategoryPalette(1, Color(0xFFE8F0FF), Color(0xFF3D5FAD)),
    CategoryPalette(2, Color(0xFFF1E9FF), Color(0xFF6B46A5)),
    CategoryPalette(3, Color(0xFFFFF0E6), Color(0xFF9B512E)),
    CategoryPalette(4, Color(0xFFFCE9F1), Color(0xFF9B3E68)),
    CategoryPalette(5, Color(0xFFE4F6F3), Color(0xFF27736C)),
    CategoryPalette(6, Color(0xFFFFF6DB), Color(0xFF866500)),
    CategoryPalette(7, Color(0xFFEFF1F5), Color(0xFF5E6470)),
    CategoryPalette(8, Color(0xFFE7F5F7), Color(0xFF28717C))
)

private val builtInCategoryPaletteIndices = mapOf(
    "FOOD" to 0,
    "TRAVEL" to 1,
    "SHOPPING" to 2,
    "BILLS" to 3,
    "ENTERTAINMENT" to 4,
    "HEALTH" to 5,
    "TRANSPORT" to 6,
    "OTHER" to 7,
    "TRANSFER" to 8
)

fun categoryPaletteFor(category: String, savedIndex: Int? = null): CategoryPalette {
    val key = category.trim().uppercase().replace(' ', '_')
    val index = builtInCategoryPaletteIndices[key]
        ?: savedIndex?.takeIf { it in categoryPalettes.indices }
        ?: Math.floorMod(key.hashCode(), categoryPalettes.size)
    return categoryPalettes[index]
}

fun categoryPaletteIndexFor(category: String): Int =
    categoryPaletteFor(category).index
