package com.example.smartrecipeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // Tüm tarifleri ana ekranda listelemek için çekiyoruz.
    // Flow kullanıyoruz çünkü veritabanında bir şey değişirse arayüz otomatik güncellensin istiyoruz.
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    // Yeni tarif eklemek için (Aynı tarif varsa üstüne yazar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    // Tarif silmek için
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}