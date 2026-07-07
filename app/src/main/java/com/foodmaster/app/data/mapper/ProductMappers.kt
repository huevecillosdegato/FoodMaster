package com.foodmaster.app.data.mapper

import com.foodmaster.app.data.local.MacrosEmbedded
import com.foodmaster.app.data.local.ProductEntity
import com.foodmaster.app.data.remote.OffProductDto
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.DataSource
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Portion
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity

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
        // Best-effort net content from OFF's free-text "quantity" (e.g. "500 g").
        netContentAmount = parseNetContent(quantity)?.amount,
        netContentUnit = parseNetContent(quantity)?.unit?.name,
    )
}

/** Build a cache entity for a manually-entered product. */
fun manualProductEntity(
    barcode: String?,
    name: String,
    brand: String?,
    servingUnit: BaseUnit,
    macros: Macros,
    id: Long = 0,
): ProductEntity = ProductEntity(
    id = id,
    barcode = barcode?.takeIf { it.isNotBlank() },
    name = name,
    brand = brand?.takeIf { it.isNotBlank() },
    category = null,
    imageUrl = null,
    macros = MacrosEmbedded(
        kcal = macros.kcal,
        protein = macros.protein,
        carbs = macros.carbs,
        fat = macros.fat,
        fiber = macros.fiber,
        sugar = macros.sugar,
        salt = macros.salt,
    ),
    servingUnit = servingUnit.name,
    source = DataSource.MANUAL.name,
    incomplete = macros.isEmpty,
    updatedAt = System.currentTimeMillis(),
)

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
    netContent = if (netContentAmount != null && netContentUnit != null) {
        Quantity(netContentAmount, unitOf(netContentUnit))
    } else {
        null
    },
    portion = if (portionLabel != null && portionAmount != null && portionUnit != null) {
        Portion(portionLabel, Quantity(portionAmount, unitOf(portionUnit)))
    } else {
        null
    },
)

/** Liquids are measured per 100 ml, everything else per 100 g. */
private fun guessServingUnit(quantity: String?): BaseUnit {
    val q = quantity?.lowercase().orEmpty()
    val looksLiquid = Regex("""\b\d+([.,]\d+)?\s*(ml|cl|l)\b""").containsMatchIn(q)
    return if (looksLiquid) BaseUnit.VOLUME else BaseUnit.MASS
}

/**
 * Parse a free-text net-content string like "500 g", "1,5 L", "330ml" into a
 * [Quantity]. Returns null when nothing recognizable is found.
 */
fun parseNetContent(quantity: String?): Quantity? {
    val q = quantity?.lowercase()?.trim() ?: return null
    val match = Regex("""(\d+(?:[.,]\d+)?)\s*(kg|g|cl|ml|l)\b""").find(q) ?: return null
    val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    return when (match.groupValues[2]) {
        "kg" -> Quantity(amount, MeasureUnit.KILOGRAM)
        "g" -> Quantity(amount, MeasureUnit.GRAM)
        "l" -> Quantity(amount, MeasureUnit.LITER)
        "cl" -> Quantity(amount * 10.0, MeasureUnit.MILLILITER)
        "ml" -> Quantity(amount, MeasureUnit.MILLILITER)
        else -> null
    }
}
