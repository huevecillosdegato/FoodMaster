package com.foodmaster.app.data.mapper

import com.foodmaster.app.data.local.MealLogEntity
import com.foodmaster.app.data.local.MealLogItemEntity
import com.foodmaster.app.data.local.MealLogWithItems
import com.foodmaster.app.domain.model.MealLog
import com.foodmaster.app.domain.model.MealLogItem
import com.foodmaster.app.domain.model.Quantity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun MealLogWithItems.toDomain(): MealLog = MealLog(
    id = meal.id,
    name = meal.name,
    dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(meal.epochMillis), ZoneId.systemDefault()),
    items = items.map { it.toDomain() },
)

fun MealLogItemEntity.toDomain(): MealLogItem = MealLogItem(
    id = id,
    productId = productId,
    name = name,
    quantity = Quantity(amount, unitOf(unit)),
    macros = macros.toDomain(),
    price = price,
)

fun MealLog.toEntity(): MealLogEntity = MealLogEntity(
    id = id,
    name = name?.takeIf { it.isNotBlank() },
    epochMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
)

fun MealLogItem.toEntity(mealLogId: Long): MealLogItemEntity = MealLogItemEntity(
    id = id,
    mealLogId = mealLogId,
    productId = productId,
    name = name,
    amount = quantity.amount,
    unit = quantity.unit.name,
    macros = macros.toEmbedded(),
    price = price,
)
