package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import com.foodmaster.app.domain.repository.InventoryRepository
import java.time.LocalDate

/** Add a (already persisted) product to the inventory, or merge into its stock. */
class AddToInventoryUseCase(
    private val inventoryRepository: InventoryRepository,
) {
    suspend operator fun invoke(
        product: Product,
        quantity: Quantity,
        location: StorageLocation,
        purchaseDate: LocalDate? = LocalDate.now(),
        expirationDate: LocalDate? = null,
        lowStockThreshold: Quantity? = null,
    ) {
        inventoryRepository.addOrIncrement(
            product = product,
            quantity = quantity,
            location = location,
            purchaseDate = purchaseDate,
            expirationDate = expirationDate,
            lowStockThreshold = lowStockThreshold,
        )
    }
}
