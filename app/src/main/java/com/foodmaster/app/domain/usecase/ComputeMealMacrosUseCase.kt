package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.Recipe

/**
 * Total macros of a recipe = Σ (product macros/100 × grams-or-ml / 100).
 *
 * When an ingredient is measured in pieces (COUNT) — or its dimension doesn't
 * match the product's per-100 basis — its contribution is skipped, since we have
 * no weight-per-piece to convert it (a Phase 3+ enhancement).
 */
class ComputeMealMacrosUseCase {

    operator fun invoke(recipe: Recipe): Macros {
        var total = Macros.ZERO
        recipe.ingredients.forEach { ingredient ->
            val factor = macroFactor(ingredient.quantity, ingredient.product.servingUnit)
            if (factor != null) {
                total += ingredient.product.macrosPer100 * factor
            }
        }
        return total
    }

    /** Factor = base amount / 100, only when the quantity dimension matches the basis. */
    private fun macroFactor(quantity: Quantity, servingBasis: BaseUnit): Double? {
        val matches = quantity.unit.base == servingBasis &&
            (servingBasis == BaseUnit.MASS || servingBasis == BaseUnit.VOLUME)
        return if (matches) quantity.toBase() / 100.0 else null
    }
}
