package com.foodmaster.app.domain.model

import java.time.LocalDate

/** A recipe: a named set of ingredients (product + quantity) and steps. */
data class Recipe(
    val id: Long,
    val name: String,
    val servings: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
)

data class RecipeIngredient(
    val product: Product,
    val quantity: Quantity,
)

/**
 * A prepared instance of a recipe. Macros (and later cost) are snapshotted at
 * preparation time because product data / prices change.
 */
data class Meal(
    val id: Long,
    val recipeId: Long,
    val recipeName: String,
    val date: LocalDate,
    val servings: Int,
    val computedMacros: Macros,
)
