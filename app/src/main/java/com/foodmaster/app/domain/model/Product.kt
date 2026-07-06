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
}

enum class DataSource { OPEN_FOOD_FACTS, MANUAL, RECEIPT }
