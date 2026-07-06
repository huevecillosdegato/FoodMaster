package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {

    fun observeAll(): Flow<List<ShoppingListItem>>

    /**
     * Idempotent auto-add from the low-stock rule: if an unchecked auto line for
     * the product already exists, its quantity is updated instead of duplicating.
     */
    suspend fun addOrMergeAuto(productId: Long, desired: Quantity)

    suspend fun addManual(productId: Long, desired: Quantity)

    suspend fun toggleChecked(id: Long, checked: Boolean)

    /** Move all checked items into the inventory, then clear them from the list. */
    suspend fun commitPurchased()

    suspend fun delete(id: Long)
}
