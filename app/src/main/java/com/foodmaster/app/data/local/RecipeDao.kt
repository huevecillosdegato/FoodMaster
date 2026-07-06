package com.foodmaster.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getWithIngredients(id: Long): RecipeWithIngredients?

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)
}

@Dao
interface MealDao {

    @Insert
    suspend fun insert(meal: MealEntity): Long

    @Query("SELECT * FROM meals WHERE date >= :fromEpochDay ORDER BY date DESC, id DESC")
    fun observeSince(fromEpochDay: Long): Flow<List<MealEntity>>
}
