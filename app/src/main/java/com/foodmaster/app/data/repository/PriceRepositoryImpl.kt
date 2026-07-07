package com.foodmaster.app.data.repository

import com.foodmaster.app.data.local.PriceDao
import com.foodmaster.app.data.local.PricePointEntity
import com.foodmaster.app.domain.model.Money
import com.foodmaster.app.domain.model.PriceSource
import com.foodmaster.app.domain.repository.PriceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate

class PriceRepositoryImpl(
    private val dao: PriceDao,
    private val io: CoroutineDispatcher,
) : PriceRepository {

    override suspend fun recordPrice(
        productId: Long,
        unitPrice: Money,
        observedAt: LocalDate,
        source: PriceSource,
    ): Unit = withContext(io) {
        dao.insert(
            PricePointEntity(
                productId = productId,
                unitPriceCents = unitPrice.cents,
                observedAt = observedAt.toEpochDay(),
                source = source.name,
            ),
        )
    }

    override suspend fun currentPrice(productId: Long): Money? = withContext(io) {
        dao.latest(productId)?.let { Money(it.unitPriceCents) }
    }
}
