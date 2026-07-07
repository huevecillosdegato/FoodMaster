package com.foodmaster.app.domain.model

/**
 * Convert a user-entered amount into how stock is tracked internally: pieces are
 * turned into grams/ml via [Product.netContent] when it's known; anything already
 * in mass/volume (or when we have no net content) is left unchanged.
 */
fun Product.resolveToStock(input: Quantity): Quantity {
    val net = netContent
    return if (input.unit == MeasureUnit.PIECE && net != null) {
        Quantity(input.amount * net.toBase(), net.unit.baseAsUnit())
    } else {
        input
    }
}

/**
 * Grams (or ml) contributed by [quantity] of this product, resolving pieces via
 * [Product.netContent]. Returns null when it can't be resolved (e.g. pieces with
 * no net content), so callers can skip the contribution.
 */
fun Product.gramsOf(quantity: Quantity): Double? = when {
    quantity.unit.base == servingUnit -> quantity.toBase()
    quantity.unit == MeasureUnit.PIECE && netContent != null -> quantity.amount * netContent.toBase()
    else -> null
}
