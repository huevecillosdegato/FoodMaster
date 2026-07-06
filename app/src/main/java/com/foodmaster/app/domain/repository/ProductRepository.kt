package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Access to products. Offline-first: implementations look in the local cache
 * first and only hit the network for barcodes not seen before.
 */
interface ProductRepository {

    /**
     * Resolve a product by its barcode.
     *
     * @return [Result.success] with the product, or `success(null)` when the
     * barcode is genuinely unknown (so the UI can offer manual entry).
     * [Result.failure] is reserved for unexpected errors.
     */
    suspend fun getByBarcode(barcode: String): Result<Product?>

    /** Fetch a cached product by id. */
    suspend fun getById(id: Long): Product?

    /** Observe a cached product by id. */
    fun observe(id: Long): Flow<Product?>

    /** Observe the full local catalog (for recipe editing, manual pick, etc.). */
    fun observeCatalog(): Flow<List<Product>>
}
