package com.foodmaster.app.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanPhase { Scanning, Loading, ProductFound, NotFound, Error }

data class ScannerUiState(
    val phase: ScanPhase = ScanPhase.Scanning,
    val scannedCode: String? = null,
    val product: Product? = null,
    val error: String? = null,
)

class ScannerViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    /** Called by the camera analyzer for each fresh detection. */
    fun onBarcode(code: String) {
        // Only react while actively scanning; ignore frames during lookup/result.
        if (_state.value.phase != ScanPhase.Scanning) return
        _state.update { it.copy(phase = ScanPhase.Loading, scannedCode = code) }

        viewModelScope.launch {
            productRepository.getByBarcode(code)
                .onSuccess { product ->
                    _state.update {
                        it.copy(
                            phase = if (product != null) ScanPhase.ProductFound else ScanPhase.NotFound,
                            product = product,
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FoodMasterApplication
                ScannerViewModel(app.container.productRepository)
            }
        }
    }
}
