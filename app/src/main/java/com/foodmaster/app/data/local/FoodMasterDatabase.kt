package com.foodmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductEntity::class,
        InventoryItemEntity::class,
        ShoppingListItemEntity::class,
        ConsumptionLogEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealEntity::class,
        MealLogEntity::class,
        MealLogItemEntity::class,
    ],
    version = 2,
        PricePointEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class FoodMasterDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealDao(): MealDao
    abstract fun mealLogDao(): MealLogDao

    companion object {
        /** v2 adds the meal-log tables (registro de comidas). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `meal_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`name` TEXT, " +
                        "`epochMillis` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_logs_epochMillis` " +
                        "ON `meal_logs` (`epochMillis`)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `meal_log_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`mealLogId` INTEGER NOT NULL, " +
                        "`productId` INTEGER, " +
                        "`name` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`unit` TEXT NOT NULL, " +
                        "`macro_kcal` REAL, " +
                        "`macro_protein` REAL, " +
                        "`macro_carbs` REAL, " +
                        "`macro_fat` REAL, " +
                        "`macro_fiber` REAL, " +
                        "`macro_sugar` REAL, " +
                        "`macro_salt` REAL, " +
                        "`price` REAL, " +
                        "FOREIGN KEY(`mealLogId`) REFERENCES `meal_logs`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_log_items_mealLogId` " +
                        "ON `meal_log_items` (`mealLogId`)",
                )
            }
        }
    }
    abstract fun priceDao(): PriceDao
}
