package com.foodmaster.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String?,
    val name: String,
    val brand: String?,
    val category: String?,
    val imageUrl: String?,
    @Embedded(prefix = "macro_") val macros: MacrosEmbedded,
    val servingUnit: String,   // MeasureUnit name
    val source: String,        // DataSource name
    val incomplete: Boolean,
    val updatedAt: Long,
    // Packaging (added in schema v2): net content + user-defined portion.
    val netContentAmount: Double? = null,
    val netContentUnit: String? = null,   // MeasureUnit name
    val portionLabel: String? = null,
    val portionAmount: Double? = null,
    val portionUnit: String? = null,      // MeasureUnit name
)

/** Flattened nutrition columns (prefixed "macro_" in the products table). */
data class MacrosEmbedded(
    val kcal: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val salt: Double?,
)
