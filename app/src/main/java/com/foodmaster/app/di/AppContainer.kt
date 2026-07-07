package com.foodmaster.app.di

import android.content.Context
import androidx.room.Room
import com.foodmaster.app.data.local.FoodMasterDatabase
import com.foodmaster.app.data.remote.OpenFoodFactsApi
import com.foodmaster.app.data.repository.InventoryRepositoryImpl
import com.foodmaster.app.data.repository.MealLogRepositoryImpl
import com.foodmaster.app.data.repository.PriceRepositoryImpl
import com.foodmaster.app.data.repository.ProductRepositoryImpl
import com.foodmaster.app.data.repository.RecipeRepositoryImpl
import com.foodmaster.app.data.repository.ShoppingListRepositoryImpl
import com.foodmaster.app.domain.repository.InventoryRepository
import com.foodmaster.app.domain.repository.MealLogRepository
import com.foodmaster.app.domain.repository.PriceRepository
import com.foodmaster.app.domain.repository.ProductRepository
import com.foodmaster.app.domain.repository.RecipeRepository
import com.foodmaster.app.domain.repository.ShoppingListRepository
import com.foodmaster.app.domain.usecase.AddToInventoryUseCase
import com.foodmaster.app.domain.usecase.ComputeMealMacrosUseCase
import com.foodmaster.app.domain.usecase.ConsumeProductUseCase
import com.foodmaster.app.domain.usecase.PrepareRecipeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Lightweight manual dependency container. Held by the [Application] and reused
 * for the app's lifetime. Kept deliberately simple; can be swapped for Hilt
 * later without touching call sites (they depend on interfaces).
 */
interface AppContainer {
    val productRepository: ProductRepository
    val inventoryRepository: InventoryRepository
    val shoppingListRepository: ShoppingListRepository
    val recipeRepository: RecipeRepository
    val mealLogRepository: MealLogRepository
    val priceRepository: PriceRepository
    val addToInventoryUseCase: AddToInventoryUseCase
    val consumeProductUseCase: ConsumeProductUseCase
    val prepareRecipeUseCase: PrepareRecipeUseCase
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val database: FoodMasterDatabase = Room.databaseBuilder(
        context.applicationContext,
        FoodMasterDatabase::class.java,
        "foodmaster.db",
    )
        .addMigrations(FoodMasterDatabase.MIGRATION_1_2)
        // Real migrations preserve data on known version jumps; destructive
        // fallback stays as a safety net for any unforeseen schema change.
        .addMigrations(*com.foodmaster.app.data.local.ALL_MIGRATIONS)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Open Food Facts asks clients to identify themselves.
            val request = chain.request().newBuilder()
                .header("User-Agent", "FoodMaster/0.1 (Android)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(OpenFoodFactsApi.BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val openFoodFactsApi: OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(
            dao = database.productDao(),
            api = openFoodFactsApi,
            io = Dispatchers.IO,
        )
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepositoryImpl(
            dao = database.inventoryDao(),
            io = Dispatchers.IO,
        )
    }

    override val shoppingListRepository: ShoppingListRepository by lazy {
        ShoppingListRepositoryImpl(
            dao = database.shoppingDao(),
            inventoryRepository = inventoryRepository,
            io = Dispatchers.IO,
        )
    }

    override val addToInventoryUseCase: AddToInventoryUseCase by lazy {
        AddToInventoryUseCase(inventoryRepository)
    }

    override val consumeProductUseCase: ConsumeProductUseCase by lazy {
        ConsumeProductUseCase(inventoryRepository, shoppingListRepository)
    }

    override val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database = database, io = Dispatchers.IO)
    }

    override val mealLogRepository: MealLogRepository by lazy {
        MealLogRepositoryImpl(database = database, io = Dispatchers.IO)
    override val priceRepository: PriceRepository by lazy {
        PriceRepositoryImpl(dao = database.priceDao(), io = Dispatchers.IO)
    }

    override val prepareRecipeUseCase: PrepareRecipeUseCase by lazy {
        PrepareRecipeUseCase(
            recipeRepository = recipeRepository,
            computeMealMacros = ComputeMealMacrosUseCase(),
            consumeProduct = consumeProductUseCase,
        )
    }
}
