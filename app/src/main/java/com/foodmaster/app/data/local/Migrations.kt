package com.foodmaster.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v2 → v3: add packaging columns to `products` (net content + user-defined
 * portion). All nullable, so existing rows migrate cleanly without data loss.
 * (v1 → v2 shipped with no schema change, hence no migration for that step.)
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN netContentAmount REAL")
        db.execSQL("ALTER TABLE products ADD COLUMN netContentUnit TEXT")
        db.execSQL("ALTER TABLE products ADD COLUMN portionLabel TEXT")
        db.execSQL("ALTER TABLE products ADD COLUMN portionAmount REAL")
        db.execSQL("ALTER TABLE products ADD COLUMN portionUnit TEXT")
    }
}

/**
 * v3 → v4: add the `price_history` table. DDL matches Room's generated schema
 * (column order, FK, index names) so the post-migration validation passes.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `price_history` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`productId` INTEGER NOT NULL, " +
                "`unitPriceCents` INTEGER NOT NULL, " +
                "`observedAt` INTEGER NOT NULL, " +
                "`source` TEXT NOT NULL, " +
                "FOREIGN KEY(`productId`) REFERENCES `products`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_history_productId` ON `price_history` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_history_observedAt` ON `price_history` (`observedAt`)")
    }
}

/** All migrations, in order. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3, MIGRATION_3_4)
