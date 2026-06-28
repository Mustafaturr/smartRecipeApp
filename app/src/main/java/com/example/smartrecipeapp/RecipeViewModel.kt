package com.example.smartrecipeapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartrecipeapp.data.AppDatabase
import com.example.smartrecipeapp.data.RecipeEntity
import com.example.smartrecipeapp.data.ShoppingItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application), RecipeViewModelContract {

    // Veritabanı ve DAO bağlantılarını başlatıyoruz
    private val database = AppDatabase.getDatabase(application)
    private val recipeDao = database.recipeDao()
    private val shoppingDao = database.shoppingDao()

    // Tüm tarifleri ekranın okuyabileceği bir "StateFlow" yapısına dönüştürüyoruz
    override val recipeList = recipeDao.getAllRecipes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Tüm alışveriş listesini ekran için hazır hale getiriyoruz
    override val shoppingList = shoppingDao.getAllShoppingItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- TARİF İŞLEMLERİ ---
    override fun addRecipe(title: String, category: String, ingredients: String, instructions: String) {
        viewModelScope.launch {
            val newRecipe = RecipeEntity(
                title = title,
                category = category,
                ingredients = ingredients,
                instructions = instructions
            )
            recipeDao.insertRecipe(newRecipe)
        }
    }

    override fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            recipeDao.deleteRecipe(recipe)
        }
    }

    // --- ALIŞVERİŞ LİSTESİ İŞLEMLERİ ---
    override fun addShoppingItem(itemName: String) {
        viewModelScope.launch {
            val newItem = ShoppingItemEntity(itemName = itemName)
            shoppingDao.insertItem(newItem)
        }
    }

    override fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            // Elemanın tıklandığında durumunu tersine çeviriyoruz (Yapıldı -> Yapılmadı)
            shoppingDao.updateItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    override fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            shoppingDao.deleteItem(item)
        }
    }

    // --- AKILLI ÖZELLİK: Tarif Malzemelerini Alışveriş Listesine Ekleme ---
    override fun addIngredientsToShoppingList(ingredientsText: String) {
        viewModelScope.launch {
            // Malzemeler virgülle ayrılmış text olduğu için onları parçalıyoruz
            val items = ingredientsText.split(",").map { it.trim() }
            for (item in items) {
                if (item.isNotEmpty()) {
                    shoppingDao.insertItem(ShoppingItemEntity(itemName = item))
                }
            }
        }
    }
}