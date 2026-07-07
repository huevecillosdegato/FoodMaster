package com.foodmaster.app.meallog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.MealLog
import com.foodmaster.app.domain.model.MealLogItem
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.repository.MealLogRepository
import com.foodmaster.app.domain.repository.ProductRepository
import com.foodmaster.app.domain.usecase.ComputePortionMacrosUseCase
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Result of looking up a scanned barcode while building a meal. */
sealed interface ScanLookup {
    data object Idle : ScanLookup
    data class Loading(val barcode: String) : ScanLookup
    data class Found(val product: Product) : ScanLookup
    data class NotFound(val barcode: String) : ScanLookup
    data class Error(val message: String?) : ScanLookup
}

class MealLogViewModel(
    private val mealLogRepository: MealLogRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val computePortionMacros = ComputePortionMacrosUseCase()

    /** History of logged meals, most recent first. */
    val history: StateFlow<List<MealLog>> = mealLogRepository.observeMealLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Items in the meal currently being built (drafts carry id = 0). */
    private val _draft = MutableStateFlow<List<MealLogItem>>(emptyList())
    val draft: StateFlow<List<MealLogItem>> = _draft.asStateFlow()

    private val _scan = MutableStateFlow<ScanLookup>(ScanLookup.Idle)
    val scan: StateFlow<ScanLookup> = _scan.asStateFlow()

    /** Running totals of the draft, recomputed from its items. */
    fun draftTotals(items: List<MealLogItem>): MealLog =
        MealLog(id = 0, name = null, dateTime = LocalDateTime.now(), items = items)

    // --- Scanning -----------------------------------------------------------

    fun onScan(barcode: String) {
        if (_scan.value !is ScanLookup.Idle) return
        _scan.value = ScanLookup.Loading(barcode)
        viewModelScope.launch {
            productRepository.getByBarcode(barcode)
                .onSuccess { product ->
                    _scan.value = if (product != null) {
                        ScanLookup.Found(product)
                    } else {
                        ScanLookup.NotFound(barcode)
                    }
                }
                .onFailure { _scan.value = ScanLookup.Error(it.message) }
        }
    }

    fun resetScan() {
        _scan.value = ScanLookup.Idle
    }

    // --- Building the draft -------------------------------------------------

    /**
     * Add a scanned catalog product; its macros are scaled from the quantity eaten.
     * The scan state is left as-is (Found) so the live camera doesn't re-detect and
     * re-add the same barcode; the caller closes the scanner, which resets it.
     */
    fun addScanned(product: Product, quantity: Quantity, price: Double?) {
        addItem(
            MealLogItem(
                id = 0,
                productId = product.id,
                name = product.name,
                quantity = quantity,
                macros = computePortionMacros(product, quantity),
                price = price,
            ),
        )
    }

    /**
     * Add a barcode-less item (e.g. an apple). The macros are the portion's macros
     * entered directly by the user, not per-100 values.
     */
    fun addManual(name: String, quantity: Quantity, portionMacros: Macros, price: Double?) {
        addItem(
            MealLogItem(
                id = 0,
                productId = null,
                name = name,
                quantity = quantity,
                macros = portionMacros,
                price = price,
            ),
        )
    }

    private fun addItem(item: MealLogItem) {
        _draft.update { it + item }
    }

    fun removeDraftItem(index: Int) {
        _draft.update { items -> items.filterIndexed { i, _ -> i != index } }
    }

    fun clearDraft() {
        _draft.value = emptyList()
        resetScan()
    }

    /** Persist the draft as a logged meal, then clear it. No-op when empty. */
    fun saveDraft(name: String?) {
        val items = _draft.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            mealLogRepository.saveMealLog(
                name = name?.trim()?.ifBlank { null },
                dateTime = LocalDateTime.now(),
                items = items,
            )
            clearDraft()
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch { mealLogRepository.deleteMealLog(id) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                MealLogViewModel(
                    mealLogRepository = container.mealLogRepository,
                    productRepository = container.productRepository,
                )
            }
        }
    }
}
