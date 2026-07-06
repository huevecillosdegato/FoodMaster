package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.InventoryItem
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InventoryRepository {

    fun observeAll(): Flow<List<InventoryItem>>

    suspend fun findByProduct(productId: Long): InventoryItem?

    /**
     * Add [product] to the inventory, or merge into the existing stock for that
     * product (adding quantities in the base unit).
     */
    suspend fun addOrIncrement(
        product: Product,
        quantity: Quantity,
        location: StorageLocation,
        purchaseDate: LocalDate?,
        expirationDate: LocalDate?,
        lowStockThreshold: Quantity?,
    )

    suspend fun updateQuantity(id: Long, quantity: Quantity)

    suspend fun logConsumption(productId: Long, consumed: Quantity, mealId: Long?)

    suspend fun delete(id: Long)
}
