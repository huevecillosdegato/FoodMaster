package com.foodmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        InventoryItemEntity::class,
        ShoppingListItemEntity::class,
        ConsumptionLogEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FoodMasterDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealDao(): MealDao
}
