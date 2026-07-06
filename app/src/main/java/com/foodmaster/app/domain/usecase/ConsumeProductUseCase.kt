package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.InventoryItem
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.baseQuantity
import com.foodmaster.app.domain.repository.InventoryRepository
import com.foodmaster.app.domain.repository.ShoppingListRepository

/**
 * Register consumption of a product: discount stock (arithmetic in the base unit
 * to avoid unit-mismatch bugs), log it, and — if stock drops to/below the
 * low-stock threshold — add the product to the shopping list (idempotently).
 */
class ConsumeProductUseCase(
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingListRepository,
) {
    suspend operator fun invoke(productId: Long, consumed: Quantity, mealId: Long? = null) {
        val item = inventoryRepository.findByProduct(productId) ?: return

        // 1. Subtract in the base unit; never go below zero.
        val remainingBase = (item.quantity.toBase() - consumed.toBase()).coerceAtLeast(0.0)
        val remaining = baseQuantity(remainingBase, item.quantity.unit.base)

        // 2. Record the consumption and the new stock.
        inventoryRepository.logConsumption(productId, consumed, mealId)
        inventoryRepository.updateQuantity(item.id, remaining)

        // 3. Replenishment rule.
        val threshold = item.lowStockThreshold
        if (threshold != null && remainingBase <= threshold.toBase()) {
            shoppingRepository.addOrMergeAuto(productId, suggestReplenishQuantity(item))
        }
    }

    /**
     * Suggested amount to re-buy. Without a stored target stock we propose the
     * threshold amount itself — enough to clear the low-stock condition.
     */
    private fun suggestReplenishQuantity(item: InventoryItem): Quantity =
        item.lowStockThreshold ?: item.quantity
}
