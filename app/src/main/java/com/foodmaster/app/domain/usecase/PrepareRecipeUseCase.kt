package com.foodmaster.app.domain.usecase

import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.repository.RecipeRepository
import java.time.LocalDate

/**
 * Prepare a recipe: snapshot its macros into a [com.foodmaster.app.domain.model.Meal],
 * then discount every ingredient from the inventory (which in turn may trigger the
 * low-stock shopping rule). Closes the inventory ↔ nutrition loop.
 */
class PrepareRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val computeMealMacros: ComputeMealMacrosUseCase,
    private val consumeProduct: ConsumeProductUseCase,
) {
    suspend operator fun invoke(recipe: Recipe, date: LocalDate = LocalDate.now()) {
        val macros = computeMealMacros(recipe)
        val mealId = recipeRepository.recordMeal(
            recipeId = recipe.id,
            recipeName = recipe.name,
            date = date,
            servings = recipe.servings,
            macros = macros,
        )
        recipe.ingredients.forEach { ingredient ->
            consumeProduct(ingredient.product.id, ingredient.quantity, mealId)
        }
    }
}
