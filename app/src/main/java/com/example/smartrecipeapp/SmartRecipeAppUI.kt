package com.example.smartrecipeapp

import com.example.smartrecipeapp.data.RecipeEntity
import com.example.smartrecipeapp.data.ShoppingItemEntity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// =====================================================================
// 1. ARABİRİM (CONTRACT) VE ÖNİZLEME MODEL TANIMLARI
// =====================================================================

interface RecipeViewModelContract {
    val recipeList: StateFlow<List<RecipeEntity>>
    val shoppingList: StateFlow<List<ShoppingItemEntity>>
    fun addRecipe(title: String, category: String, ingredients: String, instructions: String)
    fun deleteRecipe(recipe: RecipeEntity)
    fun addShoppingItem(itemName: String)
    fun toggleShoppingItem(item: ShoppingItemEntity)
    fun deleteShoppingItem(item: ShoppingItemEntity)
    fun addIngredientsToShoppingList(ingredientsText: String)
}

// Önizleme (@Preview) için gerçek veri modellerini kullanan sahte ViewModel
class MockRecipeViewModel : RecipeViewModelContract {
    override val recipeList = MutableStateFlow(
        listOf(
            RecipeEntity(1, "Mercimek Çorbası", "Çorbalar", "Kırmızı mercimek, Soğan, Havuç, Patates, Tereyağı, Nane", "Tüm malzemeleri tencereye alıp kaynatın. Ardından blenderdan geçirin ve tereyağlı nane sosu dökün."),
            RecipeEntity(2, "Sebzeli Bulgur Pilavı", "Pilavlar", "Pilavlık bulgur, Domates, Biber, Soğan, Tavuk suyu", "Soğan ve biberi kavurun. Domatesi ekleyin. Bulguru ve suyu ilave edip kısık ateşte pişirin."),
            RecipeEntity(3, "Fırın Sütlaç", "Tatlılar", "Süt, Pirinç, Şeker, Nişasta, Vanilya", "Pirinci haşlayın. Süt ve şekerle kaynatın. Nişastayı ekleyip kıvam alınca güveçlere paylaştırıp fırınlayın.")
        )
    )
    override val shoppingList = MutableStateFlow(
        listOf(
            ShoppingItemEntity(1, "Süt (2 Litre)", false),
            ShoppingItemEntity(2, "Tereyağı", true),
            ShoppingItemEntity(3, "Kırmızı Mercimek", false)
        )
    )
    override fun addRecipe(title: String, category: String, ingredients: String, instructions: String) {}
    override fun deleteRecipe(recipe: RecipeEntity) {}
    override fun addShoppingItem(itemName: String) {}
    override fun toggleShoppingItem(item: ShoppingItemEntity) {}
    override fun deleteShoppingItem(item: ShoppingItemEntity) {}
    override fun addIngredientsToShoppingList(ingredientsText: String) {}
}

// =====================================================================
// 2. MODERN ESTETİK RENK PALETİ VE TEMA
// =====================================================================

private val SageGreen = Color(0xFF4A7C59)
private val LightSage = Color(0xFFE8F0E9)
private val WarmOrange = Color(0xFFE86F51)
private val SoftPeach = Color(0xFFFDE8E4)
private val WarmCream = Color(0xFFFAF9F6)
private val DarkSlate = Color(0xFF2C3E50)
private val SubtleGray = Color(0xFF8C9BA5)
private val CardSurface = Color(0xFFFFFFFF)

@Composable
fun RecipeAppTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = SageGreen,
        onPrimary = Color.White,
        primaryContainer = LightSage,
        onPrimaryContainer = DarkSlate,
        secondary = WarmOrange,
        onSecondary = Color.White,
        secondaryContainer = SoftPeach,
        onSecondaryContainer = DarkSlate,
        background = WarmCream,
        onBackground = DarkSlate,
        surface = CardSurface,
        onSurface = DarkSlate
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// =====================================================================
// 3. ANA UYGULAMA İSKELETİ VE ALT GEZİNME (BOTTOM NAVIGATION)
// =====================================================================

enum class AppTab(val title: String, val icon: ImageVector) {
    RECIPES("Tarifler", Icons.Default.RestaurantMenu),
    SHOPPING_LIST("Alışveriş", Icons.Default.ShoppingCart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRecipeAppScreen(viewModel: RecipeViewModelContract = MockRecipeViewModel()) {
    var selectedTab by remember { mutableStateOf(AppTab.RECIPES) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var selectedRecipeForDetail by remember { mutableStateOf<RecipeEntity?>(null) }

    RecipeAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == AppTab.RECIPES) "📖 Akıllı Tarif Kitabı" else "🛒 Alışveriş Listesi",
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = WarmCream
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SageGreen,
                                selectedTextColor = SageGreen,
                                indicatorColor = LightSage,
                                unselectedIconColor = SubtleGray,
                                unselectedTextColor = SubtleGray
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == AppTab.RECIPES) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddRecipeDialog = true },
                        containerColor = WarmOrange,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tarif Ekle")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tarif Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = WarmCream
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                    when (tab) {
                        AppTab.RECIPES -> RecipesScreen(
                            viewModel = viewModel,
                            onRecipeClick = { recipe -> selectedRecipeForDetail = recipe }
                        )
                        AppTab.SHOPPING_LIST -> ShoppingListScreen(viewModel = viewModel)
                    }
                }
            }
        }

        if (showAddRecipeDialog) {
            AddRecipeDialog(
                onDismiss = { showAddRecipeDialog = false },
                onAddRecipe = { title, category, ingredients, instructions ->
                    viewModel.addRecipe(title, category, ingredients, instructions)
                    showAddRecipeDialog = false
                }
            )
        }

        selectedRecipeForDetail?.let { recipe ->
            RecipeDetailDialog(
                recipe = recipe,
                onDismiss = { selectedRecipeForDetail = null },
                onAddToShoppingList = { ingredientsText ->
                    viewModel.addIngredientsToShoppingList(ingredientsText)
                    selectedRecipeForDetail = null
                }
            )
        }
    }
}

// =====================================================================
// 4. SEKME 1: TARİFLER EKRANI VE BİLEŞENLERİ
// =====================================================================

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModelContract,
    onRecipeClick: (RecipeEntity) -> Unit
) {
    val recipeList by viewModel.recipeList.collectAsState()
    val categories = listOf("Tümü", "Çorbalar", "Ana Yemekler", "Pilavlar", "Salatalar", "Tatlılar")
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val filteredRecipes = remember(recipeList, selectedCategory) {
        if (selectedCategory == "Tümü") recipeList
        else recipeList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreen,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = Color.White,
                        labelColor = DarkSlate
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) SageGreen else Color.LightGray,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        if (filteredRecipes.isEmpty()) {
            EmptyStateView(message = "Bu kategoride henüz tarif bulunmuyor.\nSağ alttaki butondan yeni tarif ekleyebilirsiniz!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onDelete = { viewModel.deleteRecipe(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftPeach,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = recipe.category.uppercase(),
                        color = WarmOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.ingredients,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.LightGray)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Tarifi Sil",
                    tint = WarmOrange
                )
            }
        }
    }
}

// =====================================================================
// 5. SEKME 2: ALIŞVERİŞ LİSTESİ EKRANI VE BİLEŞENLERİ
// =====================================================================

@Composable
fun ShoppingListScreen(viewModel: RecipeViewModelContract) {
    val shoppingList by viewModel.shoppingList.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("El yazısıyla hızlıca malzeme ekle...", color = SubtleGray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()) {
                                viewModel.addShoppingItem(inputText.trim())
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.addShoppingItem(inputText.trim())
                            inputText = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ekle",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        if (shoppingList.isEmpty()) {
            EmptyStateView(message = "Alışveriş listeniz boş.\nYukarıdan hemen malzeme ekleyebilir veya tarif detayından aktarabilirsiniz!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shoppingList, key = { it.id }) { item ->
                    ShoppingListItemRow(
                        item = item,
                        onToggle = { viewModel.toggleShoppingItem(item) },
                        onDelete = { viewModel.deleteShoppingItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemRow(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (item.isCompleted) LightSage.copy(alpha = 0.4f) else Color.White
    val textColor = if (item.isCompleted) SubtleGray else DarkSlate
    val textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        color = backgroundColor,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SageGreen,
                        uncheckedColor = SubtleGray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textDecoration = textDecoration,
                    fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Sil",
                    tint = if (item.isCompleted) SubtleGray else WarmOrange
                )
            }
        }
    }
}

// =====================================================================
// 6. DİALOGLAR VE AÇILIR PENCERELER
// =====================================================================

@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onAddRecipe: (title: String, category: String, ingredients: String, instructions: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Çorbalar") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    val categories = listOf("Çorbalar", "Ana Yemekler", "Pilavlar", "Salatalar", "Tatlılar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🍳 Yeni Tarif Ekle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkSlate
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tarif Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SageGreen)
                )

                Text("Kategori Seçiniz:", fontSize = 12.sp, color = SubtleGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Malzemeler") },
                    placeholder = { Text("Örn: Domates, Pirinç, Tuz") },
                    supportingText = {
                        Text(
                            text = "⚠️ Lütfen malzemeleri virgülle ayırarak yazınız.",
                            color = WarmOrange,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SageGreen)
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Yapılışı") },
                    placeholder = { Text("Tarifin adım adım hazırlanışını yazınız...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SageGreen)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && ingredients.isNotBlank()) {
                        onAddRecipe(title.trim(), category, ingredients.trim(), instructions.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = DarkSlate)
            }
        },
        containerColor = WarmCream,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun RecipeDetailDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit,
    onAddToShoppingList: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftPeach
                    ) {
                        Text(
                            text = recipe.category.uppercase(),
                            color = WarmOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = SubtleGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = LightSage)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = null, tint = SageGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Malzemeler", fontWeight = FontWeight.Bold, color = DarkSlate)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val ingredientList = recipe.ingredients.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ingredientList.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(WarmOrange, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = item, style = MaterialTheme.typography.bodyMedium, color = DarkSlate)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SageGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Yapılışı", fontWeight = FontWeight.Bold, color = DarkSlate)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkSlate,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onAddToShoppingList(recipe.ingredients) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Eksik Malzemeleri Alışverişe Ekle", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =====================================================================
// 7. ORTAK YARDIMCI BİLEŞENLER
// =====================================================================

@Composable
fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = SubtleGray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = SubtleGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

// =====================================================================
// 8. ANDROID STUDIO ÖNİZLEME (PREVIEW) FONKSİYONLARI
// =====================================================================

@Preview(showBackground = true, name = "1. Ana Uygulama (Tarifler Sekmesi)")
@Composable
fun SmartRecipeAppPreview() {
    SmartRecipeAppScreen(viewModel = MockRecipeViewModel())
}

@Preview(showBackground = true, name = "2. Tarif Kartı Önizleme")
@Composable
fun RecipeCardPreview() {
    RecipeAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RecipeCard(
                recipe = RecipeEntity(
                    id = 1,
                    title = "Kremalı Mantarlı Makarna",
                    category = "Ana Yemekler",
                    ingredients = "Penne makarna, Mantar, Krema, Sarımsak, Parmesan",
                    instructions = "Makarnayı haşlayın. Mantarları soteleyip krema ekleyin ve karıştırın."
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. Alışveriş Öğesi Önizleme")
@Composable
fun ShoppingListItemPreview() {
    RecipeAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShoppingListItemRow(
                item = ShoppingItemEntity(1, "Süt (2 Litre)", false),
                onToggle = {},
                onDelete = {}
            )
            ShoppingListItemRow(
                item = ShoppingItemEntity(2, "Tereyağı (Alındı)", true),
                onToggle = {},
                onDelete = {}
            )
        }
    }
}