package com.example.ui.screens.catalog

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.ui.components.ChocolateThumbnail
import com.example.ui.components.RevelloBrandBanner
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
import com.example.ui.viewmodel.formatLKR
import com.example.util.ImageStorageUtil
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
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
                Icon(Icons.Default.Add, contentDescription = "Add New Chocolate")
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
                RevelloBrandBanner()
            }

            item {
                // Header + Search + Reset
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Product Catalog",
                                fontFamily = PlayfairFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Text(
                                text = "${products.size} handcrafted varieties • Tap price to edit quickly",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD7CCC8)
                            )
                        }

                        TextButton(
                            onClick = { viewModel.resetToDefaults() },
                            modifier = Modifier.testTag("reset_catalog_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = GoldPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search by name, shape, notes...", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldLight) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("search_catalog_input")
                    )
                }
            }

            if (products.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No chocolates found",
                            fontFamily = PlayfairFontFamily,
                            color = GoldLight,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add a chocolate with your camera/gallery photo or reset to default menu.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { productToEdit = product },
                        onQuickPriceEdit = { productForQuickPrice = product },
                        onDelete = { productToDelete = product }
                    )
                }
            }
        }
    }

    // Dialog: Add Product with picture support
    if (showAddDialog) {
        ProductFormDialog(
            dialogTitle = "Add Handcrafted Chocolate",
            initialName = "",
            initialPrice = "",
            initialDescription = "",
            initialShape = "Truffle",
            initialColorHex = "#4E2E1E",
            initialImageUri = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, desc, shape, color, imageUri ->
                viewModel.addProduct(name, price, desc, shape, color, imageUri) {
                    showAddDialog = false
                }
            }
        )
    }

    // Dialog: Full Edit Product
    productToEdit?.let { product ->
        ProductFormDialog(
            dialogTitle = "Edit: ${product.name}",
            initialName = product.name,
            initialPrice = product.price.toString(),
            initialDescription = product.description,
            initialShape = product.shape,
            initialColorHex = product.colorHex,
            initialImageUri = product.referenceImageUri,
            onDismiss = { productToEdit = null },
            onConfirm = { name, price, desc, shape, color, imageUri ->
                val updated = product.copy(
                    name = name,
                    price = price,
                    description = desc,
                    shape = shape,
                    colorHex = color,
                    referenceImageUri = imageUri
                )
                viewModel.updateProduct(updated) {
                    productToEdit = null
                }
            }
        )
    }

    // Dialog: Quick Price Edit
    productForQuickPrice?.let { product ->
        QuickPriceDialog(
            product = product,
            onDismiss = { productForQuickPrice = null },
            onSave = { newPrice ->
                viewModel.updatePrice(product.id, newPrice) {
                    productForQuickPrice = null
                }
            }
        )
    }

    // Dialog: Delete Product Confirmation
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text("Delete ${product.name}?", fontFamily = PlayfairFontFamily, color = GoldLight)
            },
            text = {
                Text(
                    "Are you sure you want to remove this chocolate from the catalog? AI recognition will no longer match against it.",
                    color = Color(0xFFD7CCC8)
                )
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
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onQuickPriceEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CacaoBorder, CacaoCard)))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real chocolate picture thumbnail (or vector shape badge if no picture)
            ChocolateThumbnail(
                shape = product.shape,
                colorHex = product.colorHex,
                imageUri = product.referenceImageUri,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Clickable Price pill for quick editing
                Surface(
                    color = CacaoSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldDark, GoldLight))),
                    modifier = Modifier
                        .clickable(onClick = onQuickPriceEdit)
                        .testTag("quick_price_${product.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                            contentDescription = "Edit Price",
                            tint = GoldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        color = Color(0xFFD7CCC8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_product_${product.name}")) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Full Item", tint = GoldLight)
            }
            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_product_${product.name}")) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RubyBerryColor)
            }
        }
    }
}

@Composable
private fun QuickPriceDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var priceInput by remember { mutableStateOf(product.price.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Price: ${product.name}", fontFamily = PlayfairFontFamily, color = GoldLight)
        },
        text = {
            Column {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = RubyBerryColor, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text("Enter new price per piece (LKR):", color = Color(0xFFD7CCC8), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it; errorMessage = null },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GoldLight,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CacaoBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("quick_price_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = priceInput.toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        errorMessage = "Please enter a valid positive amount"
                    } else {
                        onSave(parsed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                modifier = Modifier.testTag("confirm_quick_price_button")
            ) {
                Text("Update Price", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = CacaoDark
    )
}

@Composable
private fun ProductFormDialog(
    dialogTitle: String,
    initialName: String,
    initialPrice: String,
    initialDescription: String,
    initialShape: String,
    initialColorHex: String,
    initialImageUri: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, description: String, shape: String, colorHex: String, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var priceStr by remember { mutableStateOf(initialPrice) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedShape by remember { mutableStateOf(initialShape) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Camera launcher for chocolate photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageStorageUtil.saveBitmapToInternalStorage(context, bitmap, "catalog_images")
            imageUri = savedPath
        }
    }

    // Gallery launcher for chocolate photo
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

    val shapes = listOf("Truffle", "Dome", "Square", "Heart", "Rocher", "Cube")
    val colorPresets = listOf(
        "#4E2E1E" to "Dark Cacao",
        "#6B412B" to "Milk Choc",
        "#C87D32" to "Caramel",
        "#F5ECD7" to "White Ivory",
        "#396B47" to "Pistachio",
        "#B83A4B" to "Ruby Pink",
        "#2E1A11" to "Espresso"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle, fontFamily = PlayfairFontFamily, color = GoldLight)
        },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp)) {
                item {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = RubyBerryColor, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Chocolate Picture Section
                    Text("Chocolate Picture (Camera or Gallery):", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CacaoSurface, RoundedCornerShape(10.dp))
                            .border(1.dp, CacaoBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current preview (photo or shape)
                        ChocolateThumbnail(
                            shape = selectedShape,
                            colorHex = selectedColorHex,
                            imageUri = imageUri,
                            size = 56.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("chocolate_camera_photo_button")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
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
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("chocolate_gallery_photo_button")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload", fontSize = 11.sp)
                                }
                            }

                            if (imageUri != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Remove photo",
                                    color = RubyBerryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { imageUri = null }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Chocolate Name *", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price in LKR
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it; errorMessage = null },
                        label = { Text("Price per piece (LKR) *", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_price_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description (Visual cues for AI)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Visual Appearance Cues (for Gemini AI)", color = Color.Gray) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_description_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shape selector
                    Text("Shape / Model:", color = Color(0xFFD7CCC8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(shapes) { shape ->
                            val isSelected = selectedShape.equals(shape, ignoreCase = true)
                            Surface(
                                color = if (isSelected) GoldPrimary else CacaoSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.clickable { selectedShape = shape }
                            ) {
                                Text(
                                    text = shape,
                                    color = if (isSelected) CacaoBlack else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color accent picker
                    Text("Color Accent:", color = Color(0xFFD7CCC8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(colorPresets) { (hex, label) ->
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) GoldLight else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val parsedPrice = priceStr.toDoubleOrNull()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Product name cannot be empty"
                        return@Button
                    }
                    if (parsedPrice == null || parsedPrice <= 0) {
                        errorMessage = "Please enter a valid positive price"
                        return@Button
                    }
                    onConfirm(trimmedName, parsedPrice, description, selectedShape, selectedColorHex, imageUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                modifier = Modifier.testTag("save_product_form_button")
            ) {
                Text("Save Chocolate", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = CacaoDark
    )
}
