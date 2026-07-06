package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Meal
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecipeRepository {

    fun observeRecipes(): Flow<List<Recipe>>

    suspend fun getRecipe(id: Long): Recipe?

    suspend fun saveRecipe(
        name: String,
        servings: Int,
        steps: List<String>,
        ingredients: List<RecipeIngredient>,
    ): Long

    suspend fun deleteRecipe(id: Long)

    /** Meals prepared on or after [from], most recent first. */
    fun observeMealsSince(from: LocalDate): Flow<List<Meal>>

    /** Persist a prepared meal snapshot; returns its id. */
    suspend fun recordMeal(
        recipeId: Long,
        recipeName: String,
        date: LocalDate,
        servings: Int,
        macros: Macros,
    ): Long
}
