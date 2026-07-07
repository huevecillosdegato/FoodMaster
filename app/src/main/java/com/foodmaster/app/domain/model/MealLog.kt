package com.foodmaster.app.domain.model

import java.time.LocalDateTime

/**
 * A meal the user actually ate ("registro de comidas"), built ad-hoc by scanning
 * products and/or adding items by hand. Distinct from [Meal], which is a prepared
 * instance of a saved [Recipe].
 *
 * Each item snapshots its name, quantity, macro contribution and price at logging
 * time, so the record stays stable even if the underlying product is edited or
 * deleted later.
 */
data class MealLog(
    val id: Long,
    /** Optional label, e.g. "Desayuno". */
    val name: String?,
    val dateTime: LocalDateTime,
    val items: List<MealLogItem>,
) {
    /** Macros of the whole meal = Σ item contributions. Unknown fields count as 0. */
    val totalMacros: Macros
        get() = items.fold(Macros.ZERO) { acc, item -> acc + item.macros }

    /**
     * Total price of the meal, or null when no item has a price. Items without a
     * price are simply skipped, so a partial total is still meaningful.
     */
    val totalPrice: Double?
        get() {
            val priced = items.mapNotNull { it.price }
            return if (priced.isEmpty()) null else priced.sum()
        }
}

/**
 * One line of a [MealLog]: a food and how much of it was eaten, with its already
 * scaled macro contribution and (optionally) what that portion cost.
 */
data class MealLogItem(
    val id: Long,
    /** Soft reference to the catalog product, when the item came from one. */
    val productId: Long?,
    val name: String,
    val quantity: Quantity,
    /** Macros for this portion (already scaled), not per 100. */
    val macros: Macros,
    /** Euros this portion cost, or null when unknown. */
    val price: Double?,
)
