package com.foodmaster.app.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.Meal
import com.foodmaster.app.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class StatsViewModel(
    recipeRepository: RecipeRepository,
) : ViewModel() {

    val weekStart: LocalDate = LocalDate.now().minusDays(6)

    val meals: StateFlow<List<Meal>> = recipeRepository.observeMealsSince(weekStart)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                StatsViewModel(container.recipeRepository)
            }
        }
    }
}
