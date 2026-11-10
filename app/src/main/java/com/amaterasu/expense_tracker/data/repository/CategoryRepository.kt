package com.amaterasu.expense_tracker.data.repository

import com.amaterasu.expense_tracker.data.dao.CategoryDao
import com.amaterasu.expense_tracker.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

private const val EXPENSE_CATEGORY_TYPE = "EXPENSE"

private val defaultExpenseCategories = listOf(
    "FOOD" to 0,
    "GROCERY" to 9,
    "PARTY" to 10,
    "TRAVEL" to 1,
    "SHOPPING" to 2,
    "BILLS" to 3,
    "ENTERTAINMENT" to 4,
    "HEALTH" to 5,
    "TRANSPORT" to 6,
    "OTHER" to 7
)

class CategoryRepository(
    private val dao: CategoryDao
) {
    fun observeByType(type: String): Flow<List<CategoryEntity>> =
        dao.observeByType(type)

    suspend fun ensureExpenseCategories(existingNames: List<String>) {
        val createdAt = System.currentTimeMillis()
        val defaultCategories = defaultExpenseCategories.map { (name, colorIndex) ->
            CategoryEntity(
                id = CategoryEntity.idFor(name, EXPENSE_CATEGORY_TYPE),
                name = name,
                type = EXPENSE_CATEGORY_TYPE,
                colorIndex = colorIndex,
                createdAt = createdAt
            )
        }
        val existingCategories = existingNames
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .map { name ->
                CategoryEntity(
                    id = CategoryEntity.idFor(name, EXPENSE_CATEGORY_TYPE),
                    name = name,
                    type = EXPENSE_CATEGORY_TYPE,
                    colorIndex = -1,
                    createdAt = createdAt
                )
            }

        dao.insertAll(defaultCategories + existingCategories)
    }

    suspend fun addCategory(name: String, type: String, colorIndex: Int) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        dao.insert(
            CategoryEntity(
                id = CategoryEntity.idFor(trimmedName, type),
                name = trimmedName,
                type = type.trim().uppercase(),
                colorIndex = colorIndex,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
