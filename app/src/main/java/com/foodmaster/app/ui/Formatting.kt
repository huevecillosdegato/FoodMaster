package com.foodmaster.app.ui

import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Trim a trailing ".0" so 3.0 shows as "3" but 3.5 stays "3.5". */
fun formatNumber(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
}

fun Quantity.display(): String = "${formatNumber(amount)} ${unit.symbol}"

fun LocalDate.display(): String = format(dateFormatter)

fun StorageLocation.label(): String = when (this) {
    StorageLocation.NEVERA -> "Nevera"
    StorageLocation.CONGELADOR -> "Congelador"
    StorageLocation.DESPENSA -> "Despensa"
}

/** "—" when unknown, otherwise the rounded value plus [suffix]. */
fun macroValue(value: Double?, suffix: String = ""): String =
    if (value == null) "—" else "${formatNumber(value)}$suffix"

/** Compact one-line summary: "540 kcal · P 30 · C 60 · G 18". */
fun Macros.summaryLine(): String = buildString {
    append(macroValue(kcal)).append(" kcal")
    append(" · P ").append(macroValue(protein))
    append(" · C ").append(macroValue(carbs))
    append(" · G ").append(macroValue(fat))
}
