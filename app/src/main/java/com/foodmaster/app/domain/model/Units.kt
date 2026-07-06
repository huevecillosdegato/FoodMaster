package com.foodmaster.app.domain.model

import kotlin.math.roundToLong

/** Physical dimension. Base units: gram (MASS), milliliter (VOLUME), piece (COUNT). */
enum class BaseUnit { MASS, VOLUME, COUNT }

/**
 * A concrete unit the user can pick, with its conversion factor to the base unit
 * of its dimension. All inventory arithmetic is done in the base unit to avoid the
 * classic "subtract grams from kilos" bug.
 */
enum class MeasureUnit(val base: BaseUnit, val toBaseFactor: Double, val symbol: String) {
    GRAM(BaseUnit.MASS, 1.0, "g"),
    KILOGRAM(BaseUnit.MASS, 1000.0, "kg"),
    MILLILITER(BaseUnit.VOLUME, 1.0, "ml"),
    LITER(BaseUnit.VOLUME, 1000.0, "l"),
    PIECE(BaseUnit.COUNT, 1.0, "ud");

    /** The canonical unit of this unit's dimension (GRAM / MILLILITER / PIECE). */
    fun baseAsUnit(): MeasureUnit = when (base) {
        BaseUnit.MASS -> GRAM
        BaseUnit.VOLUME -> MILLILITER
        BaseUnit.COUNT -> PIECE
    }

    companion object {
        /** Units that make sense for a product measured in the given [BaseUnit]. */
        fun forDimension(base: BaseUnit): List<MeasureUnit> =
            entries.filter { it.base == base }
    }
}

/** An amount expressed in a chosen unit. */
data class Quantity(val amount: Double, val unit: MeasureUnit) {
    /** Convert to the base unit of the dimension (g, ml or pieces). */
    fun toBase(): Double = amount * unit.toBaseFactor

    operator fun plus(other: Quantity): Quantity {
        require(other.unit.base == unit.base) { "Cannot add ${other.unit} to $unit" }
        val baseTotal = toBase() + other.toBase()
        return Quantity(baseTotal / unit.toBaseFactor, unit)
    }
}

/** Build a [Quantity] in the base unit of [base] from an amount already in base units. */
fun baseQuantity(amountInBase: Double, base: BaseUnit): Quantity = when (base) {
    BaseUnit.MASS -> Quantity(amountInBase, MeasureUnit.GRAM)
    BaseUnit.VOLUME -> Quantity(amountInBase, MeasureUnit.MILLILITER)
    BaseUnit.COUNT -> Quantity(amountInBase, MeasureUnit.PIECE)
}

/** Rounded base amount, handy for persistence / comparisons. */
fun Quantity.toBaseRounded(): Long = toBase().roundToLong()

/** Where a stocked item lives. */
enum class StorageLocation { NEVERA, CONGELADOR, DESPENSA }
