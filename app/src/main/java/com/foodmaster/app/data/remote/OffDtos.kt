package com.foodmaster.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffResponseDto(
    val status: Int = 0,            // 1 = found, 0 = not found
    val product: OffProductDto? = null,
)

@Serializable
data class OffProductDto(
    val code: String? = null,
    @SerialName("product_name") val name: String? = null,
    val brands: String? = null,
    val categories: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: String? = null,
    val nutriments: OffNutrimentsDto? = null,
)

@Serializable
data class OffNutrimentsDto(
    @SerialName("energy-kcal_100g") val kcal100: Double? = null,
    @SerialName("proteins_100g") val protein100: Double? = null,
    @SerialName("carbohydrates_100g") val carbs100: Double? = null,
    @SerialName("fat_100g") val fat100: Double? = null,
    @SerialName("fiber_100g") val fiber100: Double? = null,
    @SerialName("sugars_100g") val sugar100: Double? = null,
    @SerialName("salt_100g") val salt100: Double? = null,
)
