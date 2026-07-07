package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.model.gramsOf

/**
 * Total macros of a recipe = Σ (product macros/100 × grams-or-ml / 100).
 *
 * Pieces are resolved to grams via the product's net content; ingredients that
 * still can't be resolved (pieces with no net content) are skipped.
 */
class ComputeMealMacrosUseCase {

    operator fun invoke(recipe: Recipe): Macros {
        var total = Macros.ZERO
        recipe.ingredients.forEach { ingredient ->
            val grams = ingredient.product.gramsOf(ingredient.quantity)
            if (grams != null) {
                total += ingredient.product.macrosPer100 * (grams / 100.0)
            }
        }
        return total
    }
}
