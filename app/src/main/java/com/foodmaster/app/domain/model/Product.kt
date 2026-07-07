package com.foodmaster.app.domain.model

/**
 * A food product. In the MVP it is populated either from Open Food Facts (via a
 * barcode lookup) or entered manually. Room is the source of truth; the network
 * only enriches unknown barcodes.
 */
data class Product(
    val id: Long,
    val barcode: String?,
    val name: String,
    val brand: String?,
    val category: String?,
    val imageUrl: String?,
    val macrosPer100: Macros,
    val servingUnit: BaseUnit,      // MASS (per 100 g) or VOLUME (per 100 ml)
    val source: DataSource,
    /**
     * True when key nutrition fields came back empty from the source, so the UI
     * can invite the user to complete them. Open Food Facts data is often partial.
     */
    val incomplete: Boolean,
    /**
     * Net content of one package/unit (e.g. 500 g per can). Not reliably present
     * in Open Food Facts, so the user sets it once and it is remembered by barcode.
     * Used to convert "1 unit" ↔ grams/ml.
     */
    val netContent: Quantity? = null,
    /** User-defined consumption portion (e.g. "loncha" = 10 g), remembered per product. */
    val portion: Portion? = null,
)

/**
 * A consumption portion the user defines once per product. [quantity] is always a
 * mass/volume amount (grams or ml), even when the user thinks of it as "1 slice":
 * a 100 g pack of 10 slices → Portion("loncha", 10 g).
 */
data class Portion(
    val label: String,
    val quantity: Quantity,
)

/** Nutrition per 100 g / 100 ml. Nullable fields are "unknown", not zero. */
data class Macros(
    val kcal: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val salt: Double? = null,
) {
    /** True when none of the core macros are known. */
    val isEmpty: Boolean
        get() = kcal == null && protein == null && carbs == null && fat == null

    /** Scale every known field by [factor]; unknown fields stay unknown. */
    operator fun times(factor: Double) = Macros(
        kcal = kcal?.times(factor),
        protein = protein?.times(factor),
        carbs = carbs?.times(factor),
        fat = fat?.times(factor),
        fiber = fiber?.times(factor),
        sugar = sugar?.times(factor),
        salt = salt?.times(factor),
    )

    /** Field-by-field sum. A field is null only when it is unknown on both sides. */
    operator fun plus(other: Macros) = Macros(
        kcal = add(kcal, other.kcal),
        protein = add(protein, other.protein),
        carbs = add(carbs, other.carbs),
        fat = add(fat, other.fat),
        fiber = add(fiber, other.fiber),
        sugar = add(sugar, other.sugar),
        salt = add(salt, other.salt),
    )

    companion object {
        val ZERO = Macros(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        private fun add(a: Double?, b: Double?): Double? =
            if (a == null && b == null) null else (a ?: 0.0) + (b ?: 0.0)
    }
}

enum class DataSource { OPEN_FOOD_FACTS, MANUAL, RECEIPT }
