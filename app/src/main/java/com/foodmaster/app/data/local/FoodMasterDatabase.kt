package com.foodmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FoodMasterDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
