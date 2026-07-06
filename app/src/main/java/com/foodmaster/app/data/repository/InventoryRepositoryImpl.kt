package com.foodmaster.app.data.repository

import com.foodmaster.app.data.local.ConsumptionLogEntity
import com.foodmaster.app.data.local.InventoryDao
import com.foodmaster.app.data.mapper.newInventoryEntity
import com.foodmaster.app.data.mapper.toDomain
import com.foodmaster.app.data.mapper.unitOf
import com.foodmaster.app.domain.model.InventoryItem
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import com.foodmaster.app.domain.repository.InventoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class InventoryRepositoryImpl(
    private val dao: InventoryDao,
    private val io: CoroutineDispatcher,
) : InventoryRepository {

    override fun observeAll(): Flow<List<InventoryItem>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun findByProduct(productId: Long): InventoryItem? = withContext(io) {
        dao.findWithProductByProduct(productId)?.toDomain()
    }

    override suspend fun addOrIncrement(
        product: Product,
        quantity: Quantity,
        location: StorageLocation,
        purchaseDate: LocalDate?,
        expirationDate: LocalDate?,
        lowStockThreshold: Quantity?,
    ) = withContext(io) {
        val existing = dao.findByProduct(product.id)
        if (existing != null && unitOf(existing.unit).base == quantity.unit.base) {
            // Merge in the base unit, keeping the existing display unit.
            val merged = Quantity(existing.amount, unitOf(existing.unit)) + quantity
            dao.updateQuantity(existing.id, merged.amount, merged.unit.name)
        } else {
            dao.upsert(
                newInventoryEntity(
                    productId = product.id,
                    quantity = quantity,
                    location = location,
                    purchaseDate = purchaseDate,
                    expirationDate = expirationDate,
                    lowStockThreshold = lowStockThreshold,
                    id = existing?.id ?: 0,
                ),
            )
        }
    }

    override suspend fun updateQuantity(id: Long, quantity: Quantity) = withContext(io) {
        dao.updateQuantity(id, quantity.amount, quantity.unit.name)
    }

    override suspend fun logConsumption(productId: Long, consumed: Quantity, mealId: Long?) =
        withContext(io) {
            dao.insertConsumption(
                ConsumptionLogEntity(
                    productId = productId,
                    amount = consumed.amount,
                    unit = consumed.unit.name,
                    timestamp = System.currentTimeMillis(),
                    mealId = mealId,
                ),
            )
        }

    override suspend fun itemsExpiringWithin(days: Long): List<InventoryItem> = withContext(io) {
        val max = LocalDate.now().plusDays(days).toEpochDay()
        dao.expiringBy(max).map { it.toDomain() }
    }

    override suspend fun delete(id: Long) = withContext(io) {
        dao.deleteById(id)
    }
}
