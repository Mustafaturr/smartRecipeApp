package com.example.smartrecipeapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartrecipeapp.data.RecipeDatabaseHelper
import com.example.smartrecipeapp.data.RecipeEntity
import com.example.smartrecipeapp.data.ShoppingItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeViewModel(application: Application) : AndroidViewModel(application), RecipeViewModelContract {

    private val dbHelper = RecipeDatabaseHelper(application)

    private val _recipeList = MutableStateFlow<List<RecipeEntity>>(emptyList())
    override val recipeList: StateFlow<List<RecipeEntity>> = _recipeList

    private val _shoppingList = MutableStateFlow<List<ShoppingItemEntity>>(emptyList())
    override val shoppingList: StateFlow<List<ShoppingItemEntity>> = _shoppingList

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val recipes = withContext(Dispatchers.IO) { dbHelper.getAllRecipes() }
            val shoppingItems = withContext(Dispatchers.IO) { dbHelper.getAllShoppingItems() }
            _recipeList.value = recipes
            _shoppingList.value = shoppingItems
        }
    }

    // --- TARİF İŞLEMLERİ ---
    override fun addRecipe(title: String, category: String, ingredients: String, instructions: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.insertRecipe(title, category, ingredients, instructions)
            }
            loadData()
        }
    }

    override fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteRecipe(recipe.id)
            }
            loadData()
        }
    }

    // --- ALIŞVERİŞ LİSTESİ İŞLEMLERİ ---
    override fun addShoppingItem(itemName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.insertShoppingItem(itemName)
            }
            loadData()
        }
    }

    override fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.updateShoppingItemStatus(item.id, !item.isCompleted)
            }
            loadData()
        }
    }

    override fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteShoppingItem(item.id)
            }
            loadData()
        }
    }

    // --- AKILLI ÖZELLİK: Tarif Malzemelerini Alışveriş Listesine Ekleme ---
    override fun addIngredientsToShoppingList(ingredientsText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val items = ingredientsText.split(",").map { it.trim() }
                for (item in items) {
                    if (item.isNotEmpty()) {
                        dbHelper.insertShoppingItem(item)
                    }
                }
            }
            loadData()
        }
    }
}
