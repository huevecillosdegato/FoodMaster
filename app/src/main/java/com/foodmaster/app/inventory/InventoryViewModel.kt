package com.foodmaster.app.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.InventoryItem
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.repository.InventoryRepository
import com.foodmaster.app.domain.usecase.ConsumeProductUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val consumeProductUseCase: ConsumeProductUseCase,
) : ViewModel() {

    val items: StateFlow<List<InventoryItem>> = inventoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun consume(item: InventoryItem, amount: Quantity) {
        viewModelScope.launch { consumeProductUseCase(item.product.id, amount) }
    }

    fun delete(item: InventoryItem) {
        viewModelScope.launch { inventoryRepository.delete(item.id) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                InventoryViewModel(
                    inventoryRepository = container.inventoryRepository,
                    consumeProductUseCase = container.consumeProductUseCase,
                )
            }
        }
    }
}
