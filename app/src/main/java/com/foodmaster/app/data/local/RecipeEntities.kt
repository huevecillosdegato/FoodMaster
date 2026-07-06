package com.foodmaster.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servings: Int,
    val steps: String,   // newline-joined
)

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId"), Index("productId")],
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val productId: Long,
    val amount: Double,
    val unit: String,
)

@Entity(tableName = "meals", indices = [Index("date")])
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val recipeName: String,
    val date: Long,   // epoch day
    val servings: Int,
    @Embedded(prefix = "macro_") val macros: MacrosEmbedded,
)

/** A recipe ingredient row joined with its product. */
data class IngredientWithProduct(
    @Embedded val ingredient: RecipeIngredientEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity,
)

/** A recipe with its (product-joined) ingredients. */
data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        entity = RecipeIngredientEntity::class,
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val ingredients: List<IngredientWithProduct>,
)
