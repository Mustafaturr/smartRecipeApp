package com.example.smartrecipeapp.data

data class RecipeEntity(
    val id: Int = 0,
    val title: String,
    val category: String,
    val ingredients: String,
    val instructions: String
)