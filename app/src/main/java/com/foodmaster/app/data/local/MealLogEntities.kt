package com.foodmaster.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "meal_logs", indices = [Index("epochMillis")])
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val epochMillis: Long,
)

@Entity(
    tableName = "meal_log_items",
    foreignKeys = [
        ForeignKey(
            entity = MealLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealLogId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mealLogId")],
)
data class MealLogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealLogId: Long,
    /** Soft reference — no FK, so deleting a product never wipes eaten-meal history. */
    val productId: Long?,
    val name: String,
    val amount: Double,
    val unit: String,
    @Embedded(prefix = "macro_") val macros: MacrosEmbedded,
    val price: Double?,
)

/** A logged meal with its items. */
data class MealLogWithItems(
    @Embedded val meal: MealLogEntity,
    @Relation(parentColumn = "id", entityColumn = "mealLogId")
    val items: List<MealLogItemEntity>,
)
