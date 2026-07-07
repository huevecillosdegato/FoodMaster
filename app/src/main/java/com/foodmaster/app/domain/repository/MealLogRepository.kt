package com.foodmaster.app.domain.repository

import com.foodmaster.app.domain.model.MealLog
import com.foodmaster.app.domain.model.MealLogItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MealLogRepository {

    /** All logged meals, most recent first. */
    fun observeMealLogs(): Flow<List<MealLog>>

    /** Persist a logged meal and its items in one transaction; returns its id. */
    suspend fun saveMealLog(
        name: String?,
        dateTime: LocalDateTime,
        items: List<MealLogItem>,
    ): Long

    suspend fun deleteMealLog(id: Long)
}
