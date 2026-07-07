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

/** All migrations, in order. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3)
