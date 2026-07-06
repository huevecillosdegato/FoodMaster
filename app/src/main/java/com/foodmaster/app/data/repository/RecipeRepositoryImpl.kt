package com.foodmaster.app.data.repository

import androidx.room.withTransaction
import com.foodmaster.app.data.local.FoodMasterDatabase
import com.foodmaster.app.data.mapper.ingredientEntity
import com.foodmaster.app.data.mapper.newMealEntity
import com.foodmaster.app.data.mapper.newRecipeEntity
import com.foodmaster.app.data.mapper.toDomain
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Meal
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.model.RecipeIngredient
import com.foodmaster.app.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class RecipeRepositoryImpl(
    private val database: FoodMasterDatabase,
    private val io: CoroutineDispatcher,
) : RecipeRepository {

    private val recipeDao = database.recipeDao()
    private val mealDao = database.mealDao()

    override fun observeRecipes(): Flow<List<Recipe>> =
        recipeDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getRecipe(id: Long): Recipe? = withContext(io) {
        recipeDao.getWithIngredients(id)?.toDomain()
    }

    override suspend fun saveRecipe(
        name: String,
        servings: Int,
        steps: List<String>,
        ingredients: List<RecipeIngredient>,
    ): Long = withContext(io) {
        database.withTransaction {
            val recipeId = recipeDao.insertRecipe(newRecipeEntity(name, servings, steps))
            recipeDao.insertIngredients(ingredients.map { ingredientEntity(recipeId, it) })
            recipeId
        }
    }

    override suspend fun deleteRecipe(id: Long) = withContext(io) {
        recipeDao.deleteRecipe(id)
    }

    override fun observeMealsSince(from: LocalDate): Flow<List<Meal>> =
        mealDao.observeSince(from.toEpochDay()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun recordMeal(
        recipeId: Long,
        recipeName: String,
        date: LocalDate,
        servings: Int,
        macros: Macros,
    ): Long = withContext(io) {
        mealDao.insert(newMealEntity(recipeId, recipeName, date, servings, macros))
    }
}
