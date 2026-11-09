package com.amaterasu.expense_tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String,
    val colorIndex: Int,
    val createdAt: Long
) {
    companion object {
        fun idFor(name: String, type: String): String =
            "${type.trim().uppercase()}:${name.trim().lowercase()}"
    }
}
