package com.foodmaster.app.domain.model

import java.time.LocalDate
import kotlin.math.roundToLong

/** Money in integer cents to avoid floating-point rounding. */
@JvmInline
value class Money(val cents: Long) {
    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun times(factor: Double) = Money((cents * factor).roundToLong())

    companion object {
        fun fromMajor(amount: Double) = Money((amount * 100).roundToLong())
    }
}

enum class PriceSource { MANUAL, RECEIPT }

/**
 * An observed price for a product. In the MVP the user enters it at intake
 * (MANUAL); invoice OCR will later add points with source RECEIPT. The current
 * price is the most recent point.
 */
data class PricePoint(
    val id: Long,
    val productId: Long,
    val unitPrice: Money,        // price paid for one package/unit, as entered
    val observedAt: LocalDate,
    val source: PriceSource,
)
