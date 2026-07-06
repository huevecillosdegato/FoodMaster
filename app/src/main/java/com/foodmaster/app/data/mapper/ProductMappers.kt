package com.foodmaster.app.data.mapper

import com.foodmaster.app.data.local.MacrosEmbedded
import com.foodmaster.app.data.local.ProductEntity
import com.foodmaster.app.data.remote.OffProductDto
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.DataSource
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Product

/** Map an Open Food Facts product to a cache entity for the given [barcode]. */
fun OffProductDto.toEntity(barcode: String): ProductEntity {
    val macros = MacrosEmbedded(
        kcal = nutriments?.kcal100,
        protein = nutriments?.protein100,
        carbs = nutriments?.carbs100,
        fat = nutriments?.fat100,
        fiber = nutriments?.fiber100,
        sugar = nutriments?.sugar100,
        salt = nutriments?.salt100,
    )
    val incomplete = name.isNullOrBlank() ||
        (macros.kcal == null && macros.protein == null && macros.carbs == null && macros.fat == null)

    return ProductEntity(
        barcode = barcode,
        name = name?.takeIf { it.isNotBlank() } ?: barcode,
        brand = brands?.substringBefore(',')?.trim()?.takeIf { it.isNotBlank() },
        category = categories?.substringBefore(',')?.trim()?.takeIf { it.isNotBlank() },
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        macros = macros,
        servingUnit = guessServingUnit(quantity).name,
        source = DataSource.OPEN_FOOD_FACTS.name,
        incomplete = incomplete,
        updatedAt = System.currentTimeMillis(),
    )
}

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    barcode = barcode,
    name = name,
    brand = brand,
    category = category,
    imageUrl = imageUrl,
    macrosPer100 = Macros(
        kcal = macros.kcal,
        protein = macros.protein,
        carbs = macros.carbs,
        fat = macros.fat,
        fiber = macros.fiber,
        sugar = macros.sugar,
        salt = macros.salt,
    ),
    servingUnit = runCatching { BaseUnit.valueOf(servingUnit) }.getOrDefault(BaseUnit.MASS),
    source = runCatching { DataSource.valueOf(source) }.getOrDefault(DataSource.OPEN_FOOD_FACTS),
    incomplete = incomplete,
)

/** Liquids are measured per 100 ml, everything else per 100 g. */
private fun guessServingUnit(quantity: String?): BaseUnit {
    val q = quantity?.lowercase().orEmpty()
    val looksLiquid = Regex("""\b\d+([.,]\d+)?\s*(ml|cl|l)\b""").containsMatchIn(q)
    return if (looksLiquid) BaseUnit.VOLUME else BaseUnit.MASS
}
