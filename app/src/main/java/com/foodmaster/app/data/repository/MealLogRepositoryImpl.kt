package com.foodmaster.app.data.repository

import androidx.room.withTransaction
import com.foodmaster.app.data.local.FoodMasterDatabase
import com.foodmaster.app.data.mapper.toDomain
import com.foodmaster.app.data.mapper.toEntity
import com.foodmaster.app.domain.model.MealLog
import com.foodmaster.app.domain.model.MealLogItem
import com.foodmaster.app.domain.repository.MealLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class MealLogRepositoryImpl(
    private val database: FoodMasterDatabase,
    private val io: CoroutineDispatcher,
) : MealLogRepository {

    private val dao = database.mealLogDao()

    override fun observeMealLogs(): Flow<List<MealLog>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveMealLog(
        name: String?,
        dateTime: LocalDateTime,
        items: List<MealLogItem>,
    ): Long = withContext(io) {
        database.withTransaction {
            val meal = MealLog(id = 0, name = name, dateTime = dateTime, items = emptyList())
            val mealId = dao.insertMeal(meal.toEntity())
            // Insert with id = 0 so Room autogenerates fresh item ids.
            dao.insertItems(items.map { it.copy(id = 0).toEntity(mealId) })
            mealId
        }
    }

    override suspend fun deleteMealLog(id: Long) = withContext(io) {
        dao.delete(id)
    }
}
