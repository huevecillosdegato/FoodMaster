package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity

/**
 * Macros of eating [quantity] of [product] = product macros/100 × grams-or-ml / 100.
 *
 * When the quantity's dimension doesn't match the product's per-100 basis (e.g.
 * a mass product measured in pieces), we have no conversion factor, so every macro
 * comes back unknown (null). Manual meal items sidestep this by entering the
 * portion's macros directly.
 */
class ComputePortionMacrosUseCase {

    operator fun invoke(product: Product, quantity: Quantity): Macros {
        val factor = macroFactor(quantity, product.servingUnit)
        return if (factor != null) product.macrosPer100 * factor else UNKNOWN
    }

    private fun macroFactor(quantity: Quantity, servingBasis: BaseUnit): Double? {
        val matches = quantity.unit.base == servingBasis &&
            (servingBasis == BaseUnit.MASS || servingBasis == BaseUnit.VOLUME)
        return if (matches) quantity.toBase() / 100.0 else null
    }

    private companion object {
        val UNKNOWN = Macros(kcal = null, protein = null, carbs = null, fat = null)
    }
}
