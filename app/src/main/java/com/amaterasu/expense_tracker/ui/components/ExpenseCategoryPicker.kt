package com.amaterasu.expense_tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.CategoryEntity
import com.amaterasu.expense_tracker.ui.theme.categoryPaletteFor
import com.amaterasu.expense_tracker.ui.theme.categoryPaletteIndexFor

private val suggestedExpenseCategories = listOf(
    "FOOD",
    "TRAVEL",
    "SHOPPING",
    "BILLS",
    "ENTERTAINMENT",
    "HEALTH",
    "TRANSPORT",
    "OTHER"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCategoryPicker(
    selectedCategory: String,
    availableCategories: List<CategoryEntity>,
    onCategorySelected: (String) -> Unit,
    onCategoryAdded: (String) -> Unit,
    onCategoryCreated: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var customCategories by remember { mutableStateOf(emptyList<String>()) }

    val categories = remember(availableCategories, customCategories, selectedCategory) {
        (suggestedExpenseCategories + availableCategories.map { it.name } + customCategories + selectedCategory)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Expense category",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { category ->
                val savedColorIndex = availableCategories
                    .firstOrNull { it.name.equals(category, ignoreCase = true) }
                    ?.colorIndex
                val palette = categoryPaletteFor(category, savedColorIndex)
                FilterChip(
                    selected = selectedCategory.equals(category, ignoreCase = true),
                    onClick = { onCategorySelected(category) },
                    label = { Text(categoryLabel(category)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = palette.container.copy(alpha = 0.55f),
                        labelColor = palette.content,
                        selectedContainerColor = palette.container,
                        selectedLabelColor = palette.content
                    )
                )
            }
        }

        TextButton(
            onClick = {
                showNewCategoryInput = !showNewCategoryInput
                if (!showNewCategoryInput) newCategoryName = ""
            }
        ) {
            Text(if (showNewCategoryInput) "Cancel" else "+ Add category")
        }

        if (showNewCategoryInput) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("New category") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    enabled = newCategoryName.isNotBlank(),
                    onClick = {
                        val enteredCategory = newCategoryName.trim()
                        val existingCategory = categories.firstOrNull {
                            it.equals(enteredCategory, ignoreCase = true)
                        }
                        val categoryToSelect = existingCategory ?: enteredCategory
                        if (existingCategory == null) {
                            customCategories = customCategories + enteredCategory
                            onCategoryCreated(
                                enteredCategory,
                                categoryPaletteIndexFor(enteredCategory)
                            )
                        }
                        onCategoryAdded(categoryToSelect)
                        newCategoryName = ""
                        showNewCategoryInput = false
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}

private fun categoryLabel(category: String): String =
    category
        .lowercase()
        .replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { character -> character.titlecase() }
        }
