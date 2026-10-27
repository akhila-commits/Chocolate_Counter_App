package com.example.ui.screens.catalog

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PRODUCT_CATEGORIES
import com.example.data.model.Product
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ChocolateThumbnail
import com.example.ui.components.RevelloBrandBanner
import com.example.ui.components.SizeWeightBadge
import com.example.ui.components.formatLKR
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AmberCaramel
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.CacaoSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.RubyBerryColor
import com.example.ui.viewmodel.CatalogViewModel
import com.example.util.ImageStorageUtil
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForQuickPrice by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val categoriesWithAll = remember { listOf("All") + PRODUCT_CATEGORIES }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CacaoBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = CacaoBlack,
                shape = CircleShape,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Product")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                RevelloBrandBanner(
                    title = "PRODUCT CATALOG",
                    subtitle = "Manage varieties, categories, weights & prices"
                )
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shop Products",
                                fontFamily = PlayfairFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Text(
                                text = "${products.size} items • Tap price to edit quickly",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD7CCC8)
                            )
                        }

                        TextButton(
                            onClick = { viewModel.resetToDefaults() },
                            modifier = Modifier.testTag("reset_catalog_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Default Menu", color = GoldPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_search_bar"),
                        placeholder = {
                            Text("Search catalog (e.g. 100g, slab, cake, gelato)...", color = Color(0xFF9E8E81), fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFD7CCC8))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesWithAll) { category ->
                            val isSelected = (selectedCategory ?: "All").equals(category, ignoreCase = true)
                            val count = if (category == "All") allProducts.size else allProducts.count { it.category.equals(category, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategorySelected(category) },
                                label = {
                                    Text(
                                        text = "$category ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    if (category != "All") {
                                        Icon(
                                            imageVector = getCategoryIcon(category),
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = CacaoDark,
                                    labelColor = Color(0xFFD7CCC8),
                                    iconColor = Color(0xFFD7CCC8),
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = CacaoBlack,
                                    selectedLeadingIconColor = CacaoBlack
                                ),
                                border = BorderStroke(1.dp, if (isSelected) GoldPrimary else CacaoBorder)
                            )
                        }
                    }
                }
            }

            if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No products found in this category.\nTap '+' to add a new product.",
                            color = Color(0xFF9E8E81),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    CatalogProductCard(
                        product = product,
                        onEdit = { productToEdit = product },
                        onQuickPrice = { productForQuickPrice = product },
                        onDelete = { productToDelete = product }
                    )
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            dialogTitle = "Add New Product",
            initialName = "",
            initialCategory = if (selectedCategory != null && selectedCategory != "All") selectedCategory!! else "Chocolate pieces",
            initialSize = "",
            initialPrice = "",
            initialDescription = "",
            initialShape = "Piece",
            initialColorHex = "#4E2E1E",
            initialImageUri = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, size, price, desc, shape, colorHex, imgUri ->
                viewModel.addProduct(
                    name = name,
                    category = category,
                    size = size,
                    price = price,
                    description = desc,
                    shape = shape,
                    colorHex = colorHex,
                    referenceImageUri = imgUri
                ) {
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        AddEditProductDialog(
            dialogTitle = "Edit Product Details",
            initialName = product.name,
            initialCategory = product.category,
            initialSize = product.size,
            initialPrice = product.price.toString(),
            initialDescription = product.description,
            initialShape = product.shape,
            initialColorHex = product.colorHex,
            initialImageUri = product.referenceImageUri,
            onDismiss = { productToEdit = null },
            onConfirm = { name, category, size, price, desc, shape, colorHex, imgUri ->
                viewModel.updateProduct(
                    product.copy(
                        name = name,
                        category = category,
                        size = size,
                        price = price,
                        description = desc,
                        shape = shape,
                        colorHex = colorHex,
                        referenceImageUri = imgUri
                    )
                ) {
                    productToEdit = null
                }
            }
        )
    }

    // Quick Price Dialog
    productForQuickPrice?.let { product ->
        QuickPriceEditDialog(
            product = product,
            onDismiss = { productForQuickPrice = null },
            onConfirm = { newPrice ->
                viewModel.updatePrice(product.id, newPrice) {
                    productForQuickPrice = null
                }
            }
        )
    }

    // Delete Confirmation
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = CacaoCard,
            title = {
                Text("Delete ${product.displayTitle}?", color = GoldLight, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove this product from the catalog?", color = Color(0xFFD7CCC8))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyBerryColor, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = GoldLight)
                }
            }
        )
    }
}

@Composable
private fun CatalogProductCard(
    product: Product,
    onEdit: () -> Unit,
    onQuickPrice: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("catalog_item_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CacaoCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CacaoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChocolateThumbnail(
                shape = product.shape,
                colorHex = product.colorHex,
                imageUri = product.referenceImageUri,
                category = product.category,
                size = 50.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = product.category)

                    if (product.size.isNotBlank()) {
                        SizeWeightBadge(size = product.size)
                    }
                }

                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        color = Color(0xFFBCAAA4),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price badge (clickable for quick edit)
                Surface(
                    color = CacaoDark,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onQuickPrice() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatLKR(product.price),
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit price",
                            tint = GoldPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // Edit & Delete actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit details",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = RubyBerryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProductDialog(
    dialogTitle: String,
    initialName: String,
    initialCategory: String,
    initialSize: String,
    initialPrice: String,
    initialDescription: String,
    initialShape: String,
    initialColorHex: String,
    initialImageUri: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, size: String, price: Double, description: String, shape: String, colorHex: String, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory.ifEmpty { "Chocolate pieces" }) }
    var size by remember { mutableStateOf(initialSize) }
    var priceStr by remember { mutableStateOf(initialPrice) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedShape by remember { mutableStateOf(initialShape) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageStorageUtil.saveBitmapToInternalStorage(context, bitmap, "catalog_images")
            imageUri = savedPath
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                errorMessage = "Unable to start camera: ${e.localizedMessage}"
            }
        } else {
            errorMessage = "Camera permission needed for photo capture"
        }
    }

    val requestCameraAndLaunch = {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                errorMessage = "Unable to start camera: ${e.localizedMessage}"
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageUtil.saveUriToInternalStorage(context, uri, "catalog_images")
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    val commonWeightSuggestions = listOf("100g", "200g", "250g", "500g", "1kg", "1 Pc", "Regular", "Large", "500ml", "Box of 12")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CacaoCard,
        title = {
            Text(dialogTitle, fontFamily = PlayfairFontFamily, color = GoldLight, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = RubyBerryColor, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Product Picture Section
                    Text("Product Picture:", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CacaoDark, RoundedCornerShape(8.dp))
                            .border(1.dp, CacaoBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChocolateThumbnail(
                            shape = selectedShape,
                            colorHex = selectedColorHex,
                            imageUri = imageUri,
                            category = category,
                            size = 50.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = requestCameraAndLaunch,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 11.sp)
                            }

                            if (imageUri != null) {
                                TextButton(
                                    onClick = { imageUri = null },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Remove", color = RubyBerryColor, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Product Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *", color = Color(0xFFD7CCC8)) },
                        placeholder = { Text("e.g. 70% Dark Chocolate Slab", color = Color(0xFF8D6E63)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Category Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product Category *", color = Color(0xFFD7CCC8)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("product_category_dropdown"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CacaoDark,
                                unfocusedContainerColor = CacaoDark,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CacaoBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier.background(CacaoCard)
                        ) {
                            PRODUCT_CATEGORIES.forEach { catOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = getCategoryIcon(catOption),
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(catOption, color = Color.White)
                                        }
                                    },
                                    onClick = {
                                        category = catOption
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Size / Weight
                item {
                    Column {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Product Size / Weight (e.g. 100g, 250g, 1 Pc) *", color = Color(0xFFD7CCC8)) },
                            placeholder = { Text("100g", color = Color(0xFF8D6E63)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_size_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CacaoDark,
                                unfocusedContainerColor = CacaoDark,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CacaoBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick suggestion pills
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(commonWeightSuggestions) { sug ->
                                Surface(
                                    modifier = Modifier.clickable { size = sug },
                                    color = CacaoDark,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, AmberCaramel.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = sug,
                                        color = AmberCaramel,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Price (LKR)
                item {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (LKR) *", color = Color(0xFFD7CCC8)) },
                        placeholder = { Text("650.00", color = Color(0xFF8D6E63)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_price_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)", color = Color(0xFFD7CCC8)) },
                        placeholder = { Text("Ingredients, chocolate origin, tasting notes...", color = Color(0xFF8D6E63)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Product name cannot be empty"
                    } else if (p == null || p <= 0) {
                        errorMessage = "Please enter a valid price in LKR"
                    } else {
                        onConfirm(name.trim(), category, size.trim(), p, description.trim(), selectedShape, selectedColorHex, imageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Save Product", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoldLight)
            }
        }
    )
}

@Composable
private fun QuickPriceEditDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf(product.price.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CacaoCard,
        title = {
            Text("Update Price for ${product.displayTitle}", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text("Enter new price in LKR:", color = Color(0xFFD7CCC8), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        error = null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CacaoDark,
                        unfocusedContainerColor = CacaoDark,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CacaoBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error!!, color = RubyBerryColor, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull()
                    if (p != null && p > 0) {
                        onConfirm(p)
                    } else {
                        error = "Enter a positive price"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
            ) {
                Text("Update Price")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoldLight)
            }
        }
    )
}
