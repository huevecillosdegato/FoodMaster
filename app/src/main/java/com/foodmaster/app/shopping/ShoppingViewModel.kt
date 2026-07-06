package com.foodmaster.app.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.ShoppingListItem
import com.foodmaster.app.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val repository: ShoppingListRepository,
) : ViewModel() {

    val items: StateFlow<List<ShoppingListItem>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch { repository.toggleChecked(item.id, !item.checked) }
    }

    fun delete(item: ShoppingListItem) {
        viewModelScope.launch { repository.delete(item.id) }
    }

    /** Move all checked items into the inventory and clear them from the list. */
    fun commitPurchased() {
        viewModelScope.launch { repository.commitPurchased() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                ShoppingViewModel(container.shoppingListRepository)
            }
        }
    }
}
