package com.foodmaster.app.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodmaster.app.FoodMasterApplication
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.domain.model.RecipeIngredient
import com.foodmaster.app.domain.repository.ProductRepository
import com.foodmaster.app.domain.repository.RecipeRepository
import com.foodmaster.app.domain.usecase.ComputeMealMacrosUseCase
import com.foodmaster.app.domain.usecase.PrepareRecipeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val recipeRepository: RecipeRepository,
    productRepository: ProductRepository,
    private val prepareRecipeUseCase: PrepareRecipeUseCase,
) : ViewModel() {

    private val computeMacros = ComputeMealMacrosUseCase()

    val recipes: StateFlow<List<Recipe>> = recipeRepository.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val catalog: StateFlow<List<Product>> = productRepository.observeCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun macrosOf(recipe: Recipe): Macros = computeMacros(recipe)

    fun prepare(recipe: Recipe) {
        viewModelScope.launch { prepareRecipeUseCase(recipe) }
    }

    fun delete(recipe: Recipe) {
        viewModelScope.launch { recipeRepository.deleteRecipe(recipe.id) }
    }

    fun save(name: String, servings: Int, steps: List<String>, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch { recipeRepository.saveRecipe(name, servings, steps, ingredients) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as FoodMasterApplication).container
                RecipesViewModel(
                    recipeRepository = container.recipeRepository,
                    productRepository = container.productRepository,
                    prepareRecipeUseCase = container.prepareRecipeUseCase,
                )
            }
        }
    }
}
