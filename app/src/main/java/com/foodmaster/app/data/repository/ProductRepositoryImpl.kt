package com.foodmaster.app.data.repository

import com.foodmaster.app.data.local.ProductDao
import com.foodmaster.app.data.mapper.toDomain
import com.foodmaster.app.data.mapper.toEntity
import com.foodmaster.app.data.remote.OpenFoodFactsApi
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Offline-first product repository.
 *
 * Lookup order for [getByBarcode]: local cache → Open Food Facts → cache the
 * result. A network failure never crashes the flow; it surfaces as
 * [Result.failure] so the UI can offer manual entry / retry.
 */
class ProductRepositoryImpl(
    private val dao: ProductDao,
    private val api: OpenFoodFactsApi,
    private val io: CoroutineDispatcher,
) : ProductRepository {

    override suspend fun getByBarcode(barcode: String): Result<Product?> = withContext(io) {
        // 1) Cache hit.
        dao.findByBarcode(barcode)?.let { return@withContext Result.success(it.toDomain()) }

        // 2) Network enrichment.
        runCatching { api.getProduct(barcode) }.map { response ->
            val dto = response.product
            if (response.status == 1 && dto != null) {
                val entity = dto.toEntity(barcode)
                val id = dao.upsert(entity)
                entity.copy(id = id).toDomain()
            } else {
                null // genuinely unknown barcode → UI offers manual entry
            }
        }
    }

    override fun observe(id: Long): Flow<Product?> =
        dao.observeById(id).map { it?.toDomain() }
}
