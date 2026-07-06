package com.foodmaster.app.data.mapper

import com.foodmaster.app.data.local.MacrosEmbedded
import com.foodmaster.app.data.local.MealEntity
import com.foodmaster.app.data.local.RecipeEntity
import com.foodmaster.app.data.local.RecipeIngredientEntity
import com.foodmaster.app.data.local.RecipeWithIngredients
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Meal
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.model.RecipeIngredient
import java.time.LocalDate

fun RecipeWithIngredients.toDomain(): Recipe = Recipe(
    id = recipe.id,
    name = recipe.name,
    servings = recipe.servings,
    ingredients = ingredients.map {
        RecipeIngredient(
            product = it.product.toDomain(),
            quantity = Quantity(it.ingredient.amount, unitOf(it.ingredient.unit)),
        )
    },
    steps = recipe.steps.split("\n").filter { it.isNotBlank() },
)

fun newRecipeEntity(name: String, servings: Int, steps: List<String>): RecipeEntity =
    RecipeEntity(name = name, servings = servings, steps = steps.joinToString("\n"))

fun ingredientEntity(recipeId: Long, ingredient: RecipeIngredient): RecipeIngredientEntity =
    RecipeIngredientEntity(
        recipeId = recipeId,
        productId = ingredient.product.id,
        amount = ingredient.quantity.amount,
        unit = ingredient.quantity.unit.name,
    )

fun MealEntity.toDomain(): Meal = Meal(
    id = id,
    recipeId = recipeId,
    recipeName = recipeName,
    date = LocalDate.ofEpochDay(date),
    servings = servings,
    computedMacros = macros.toDomain(),
)

fun newMealEntity(
    recipeId: Long,
    recipeName: String,
    date: LocalDate,
    servings: Int,
    macros: Macros,
): MealEntity = MealEntity(
    recipeId = recipeId,
    recipeName = recipeName,
    date = date.toEpochDay(),
    servings = servings,
    macros = macros.toEmbedded(),
)

fun MacrosEmbedded.toDomain(): Macros =
    Macros(kcal, protein, carbs, fat, fiber, sugar, salt)

fun Macros.toEmbedded(): MacrosEmbedded =
    MacrosEmbedded(kcal, protein, carbs, fat, fiber, sugar, salt)
