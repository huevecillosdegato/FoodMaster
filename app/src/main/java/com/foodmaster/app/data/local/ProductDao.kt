package com.foodmaster.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query(
        "UPDATE products SET " +
            "netContentAmount = :netAmount, netContentUnit = :netUnit, " +
            "portionLabel = :portionLabel, portionAmount = :portionAmount, portionUnit = :portionUnit " +
            "WHERE id = :id",
    )
    suspend fun updatePackaging(
        id: Long,
        netAmount: Double?,
        netUnit: String?,
        portionLabel: String?,
        portionAmount: Double?,
        portionUnit: String?,
    )

    @Upsert
    suspend fun upsert(product: ProductEntity): Long
}
