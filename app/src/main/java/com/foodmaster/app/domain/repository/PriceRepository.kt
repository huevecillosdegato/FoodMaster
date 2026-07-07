package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.Money
import com.foodmaster.app.domain.model.PriceSource
import java.time.LocalDate

interface PriceRepository {

    /** Record an observed price for a product. */
    suspend fun recordPrice(
        productId: Long,
        unitPrice: Money,
        observedAt: LocalDate = LocalDate.now(),
        source: PriceSource = PriceSource.MANUAL,
    )

    /** The most recent price for a product, or null if none recorded. */
    suspend fun currentPrice(productId: Long): Money?
}
