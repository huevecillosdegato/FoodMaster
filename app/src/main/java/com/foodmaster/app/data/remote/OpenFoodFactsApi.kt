package com.foodmaster.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Open Food Facts v2 product endpoint.
 *
 * `GET /api/v2/product/{barcode}.json?fields=...`
 */
interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = DEFAULT_FIELDS,
    ): OffResponseDto

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
        const val DEFAULT_FIELDS =
            "code,product_name,brands,categories,image_url,nutriments,quantity"
    }
}
