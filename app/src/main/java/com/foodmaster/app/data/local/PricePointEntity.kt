package com.foodmaster.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("productId"), Index("observedAt")],
)
data class PricePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val unitPriceCents: Long,
    val observedAt: Long,   // epoch day
    val source: String,     // PriceSource name
)

@Dao
interface PriceDao {

    @Insert
    suspend fun insert(point: PricePointEntity): Long

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY observedAt DESC, id DESC LIMIT 1")
    suspend fun latest(productId: Long): PricePointEntity?
}
