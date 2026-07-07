package com.foodmaster.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {

    @Transaction
    @Query("SELECT * FROM meal_logs ORDER BY epochMillis DESC, id DESC")
    fun observeAll(): Flow<List<MealLogWithItems>>

    @Insert
    suspend fun insertMeal(meal: MealLogEntity): Long

    @Insert
    suspend fun insertItems(items: List<MealLogItemEntity>)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun delete(id: Long)
}
