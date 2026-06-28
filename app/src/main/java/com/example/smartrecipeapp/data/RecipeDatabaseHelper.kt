package com.example.smartrecipeapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RecipeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    companion object {
        const val DATABASE_NAME = "RecipeApp.db"

        // Recipes Table
        const val TABLE_RECIPES = "recipes"
        const val COL_RECIPE_ID = "id"
        const val COL_RECIPE_TITLE = "title"
        const val COL_RECIPE_CATEGORY = "category"
        const val COL_RECIPE_INGREDIENTS = "ingredients"
        const val COL_RECIPE_INSTRUCTIONS = "instructions"

        // Shopping List Table
        const val TABLE_SHOPPING = "shopping_list"
        const val COL_SHOPPING_ID = "id"
        const val COL_SHOPPING_ITEM_NAME = "item_name"
        const val COL_SHOPPING_IS_COMPLETED = "is_completed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createRecipesTable = """
            CREATE TABLE $TABLE_RECIPES (
                $COL_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RECIPE_TITLE TEXT NOT NULL,
                $COL_RECIPE_CATEGORY TEXT NOT NULL,
                $COL_RECIPE_INGREDIENTS TEXT NOT NULL,
                $COL_RECIPE_INSTRUCTIONS TEXT NOT NULL
            )
        """.trimIndent()

        val createShoppingTable = """
            CREATE TABLE $TABLE_SHOPPING (
                $COL_SHOPPING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SHOPPING_ITEM_NAME TEXT NOT NULL,
                $COL_SHOPPING_IS_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        db?.execSQL(createRecipesTable)
        db?.execSQL(createShoppingTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SHOPPING")
        onCreate(db)
    }

    // Recipe Operations
    fun insertRecipe(title: String, category: String, ingredients: String, instructions: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_RECIPE_TITLE, title)
            put(COL_RECIPE_CATEGORY, category)
            put(COL_RECIPE_INGREDIENTS, ingredients)
            put(COL_RECIPE_INSTRUCTIONS, instructions)
        }
        val result = db.insert(TABLE_RECIPES, null, values)
        db.close()
        return result != -1L
    }

    fun getAllRecipes(): List<RecipeEntity> {
        val recipeList = ArrayList<RecipeEntity>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECIPES", null)

        if (cursor.moveToFirst()) {
            do {
                val recipe = RecipeEntity(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    category = cursor.getString(2),
                    ingredients = cursor.getString(3),
                    instructions = cursor.getString(4)
                )
                recipeList.add(recipe)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return recipeList
    }

    fun deleteRecipe(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_RECIPES, "$COL_RECIPE_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // Shopping List Operations
    fun insertShoppingItem(itemName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SHOPPING_ITEM_NAME, itemName)
            put(COL_SHOPPING_IS_COMPLETED, 0)
        }
        val result = db.insert(TABLE_SHOPPING, null, values)
        db.close()
        return result != -1L
    }

    fun getAllShoppingItems(): List<ShoppingItemEntity> {
        val itemList = ArrayList<ShoppingItemEntity>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SHOPPING", null)

        if (cursor.moveToFirst()) {
            do {
                val item = ShoppingItemEntity(
                    id = cursor.getInt(0),
                    itemName = cursor.getString(1),
                    isCompleted = cursor.getInt(2) == 1
                )
                itemList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return itemList
    }

    fun updateShoppingItemStatus(id: Int, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SHOPPING_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val result = db.update(TABLE_SHOPPING, values, "$COL_SHOPPING_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun deleteShoppingItem(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_SHOPPING, "$COL_SHOPPING_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}
