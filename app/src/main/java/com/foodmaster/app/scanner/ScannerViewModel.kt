package com.foodmaster.app.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Portion
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import com.foodmaster.app.domain.repository.InventoryRepository
import com.foodmaster.app.domain.repository.ProductRepository
import com.foodmaster.app.domain.usecase.AddToInventoryUseCase
import com.foodmaster.app.domain.usecase.ConsumeProductUseCase
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanPhase { Scanning, Loading, ProductFound, NotFound, Error }

data class ScannerUiState(
    val phase: ScanPhase = ScanPhase.Scanning,
    val scannedCode: String? = null,
    val product: Product? = null,
    val stock: Quantity? = null,   // current inventory stock for the found product
    val error: String? = null,
)

class ScannerViewModel(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val addToInventoryUseCase: AddToInventoryUseCase,
    private val consumeProductUseCase: ConsumeProductUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    // One-shot user messages (toasts).
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    /** Called by the camera analyzer for each fresh detection. */
    fun onBarcode(code: String) {
        // Only react while actively scanning; ignore frames during lookup/result.
        if (_state.value.phase != ScanPhase.Scanning) return
        _state.update { it.copy(phase = ScanPhase.Loading, scannedCode = code) }

        viewModelScope.launch {
            productRepository.getByBarcode(code)
                .onSuccess { product ->
                    // For a known product, also load its current stock (for Consumo).
                    val stock = product?.let { inventoryRepository.findByProduct(it.id)?.quantity }
                    _state.update {
                        it.copy(
                            phase = if (product != null) ScanPhase.ProductFound else ScanPhase.NotFound,
                            product = product,
                            stock = stock,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(phase = ScanPhase.Error, error = e.message) }
                }
        }
    }

    /** Return to live scanning, clearing the last result. */
    fun scanAgain() {
        _state.value = ScannerUiState()
    }

    /**
     * Create a manual product for the barcode that wasn't found, then move to the
     * ProductFound phase so the normal "add to inventory" flow can continue.
     */
    fun createManualProduct(name: String, brand: String?, servingUnit: BaseUnit, macros: Macros) {
        val code = _state.value.scannedCode
        viewModelScope.launch {
            val product = productRepository.saveManual(code, name, brand, servingUnit, macros)
            _state.update { it.copy(phase = ScanPhase.ProductFound, product = product) }
        }
    }

    /** Add the currently found product to the inventory, then resume scanning. */
    fun addCurrentToInventory(
        quantity: Quantity,
        location: StorageLocation,
        expirationDate: LocalDate?,
        lowStockThreshold: Quantity?,
        netContent: Quantity?,
        portion: Portion?,
    ) {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            // Remember packaging on the product so the next scan pre-fills it.
            if (netContent != null || portion != null) {
                productRepository.updatePackaging(product.id, netContent, portion)
            }
            addToInventoryUseCase(
                product = product,
                quantity = quantity,
                location = location,
                expirationDate = expirationDate,
                lowStockThreshold = lowStockThreshold,
            )
            _messages.tryEmit("${product.name} añadido al inventario")
            scanAgain()
        }
    }

    /** Register consumption of the currently scanned product from stock. */
    fun consumeScanned(quantity: Quantity) {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val item = inventoryRepository.findByProduct(product.id)
            if (item != null) {
                consumeProductUseCase(product.id, quantity)
                _messages.tryEmit("Consumido: ${product.name}")
            } else {
                _messages.tryEmit("No tienes stock de ${product.name}")
            }
            scanAgain()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                ScannerViewModel(
                    productRepository = container.productRepository,
                    inventoryRepository = container.inventoryRepository,
                    addToInventoryUseCase = container.addToInventoryUseCase,
                    consumeProductUseCase = container.consumeProductUseCase,
                )
            }
        }
    }
}
