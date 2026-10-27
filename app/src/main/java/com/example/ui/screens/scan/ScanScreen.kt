package com.example.ui.screens.scan

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PRODUCT_CATEGORIES
import com.example.data.model.Product
import com.example.data.model.ScanItem
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ChocolateThumbnail
import com.example.ui.components.QuantityStepper
import com.example.ui.components.RevelloBrandBanner
import com.example.ui.components.SizeWeightBadge
import com.example.ui.components.formatLKR
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.ScanViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    catalogProducts: List<Product>,
    modifier: Modifier = Modifier
) {
    val orderNumber by viewModel.orderNumber.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val scanItems by viewModel.scanItems.collectAsStateWithLifecycle()
    val tenderAmount by viewModel.tenderAmount.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showEditOrderDialog by rememberSaveable { mutableStateOf(false) }
    var showOrderCalculatorDialog by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showReceiptDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val displayedProducts by remember(catalogProducts, searchQuery, selectedCategory) {
        derivedStateOf {
            var list = catalogProducts
            if (!selectedCategory.isNullOrBlank() && selectedCategory != "All") {
                list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim()
                list = list.filter {
                    it.name.contains(q, ignoreCase = true) ||
                    it.size.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    it.description.contains(q, ignoreCase = true)
                }
            }
            list
        }
    }

    val categoriesWithAll = remember { listOf("All") + PRODUCT_CATEGORIES }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CacaoBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (scanItems.isNotEmpty()) {
                BottomOrderSummaryBar(
                    orderNumber = orderNumber,
                    totalPieces = viewModel.totalPieces,
                    typesCount = viewModel.typesCount,
                    grandTotal = viewModel.grandTotal,
                    onOpenCalculator = { showOrderCalculatorDialog = true }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RevelloBrandBanner(
                title = "REVELLO ORDER CALCULATOR",
                subtitle = "Kalutara Counter • Fast Product Tally & POS"
            )

            OrderHeaderBar(
                orderNumber = orderNumber,
                grandTotal = viewModel.grandTotal,
                onEditOrderNumber = { showEditOrderDialog = true },
                onNewOrder = { viewModel.generateNewOrderNumber() },
                onOpenCalculator = { showOrderCalculatorDialog = true },
                hasItems = scanItems.isNotEmpty()
            )

            ProductSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClear = { viewModel.onSearchQueryChanged("") }
            )

            CategoryFilterChips(
                categories = categoriesWithAll,
                selectedCategory = selectedCategory ?: "All",
                onSelectCategory = { viewModel.onCategorySelected(it) },
                products = catalogProducts
            )

            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF8D6E63),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No products matching \"$searchQuery\"" else "No products in this category",
                            color = Color(0xFFD7CCC8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search another keyword or check the Catalog tab",
                            color = Color(0xFFA1887F),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("order_product_list"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = displayedProducts,
                        key = { "${it.id}-${it.size}" }
                    ) { product ->
                        val currentCount = viewModel.getItemCountForProduct(product)
                        ProductOrderRow(
                            product = product,
                            currentCount = currentCount,
                            onAdd = { viewModel.addProductToOrder(product, 1) },
                            onIncrement = {
                                val item = scanItems.firstOrNull {
                                    (it.productId != 0L && it.productId == product.id) ||
                                    (it.productName.equals(product.name, ignoreCase = true) && it.size.equals(product.size, ignoreCase = true))
                                }
                                if (item != null) {
                                    viewModel.incrementCount(item)
                                } else {
                                    viewModel.addProductToOrder(product, 1)
                                }
                            },
                            onDecrement = {
                                val item = scanItems.firstOrNull {
                                    (it.productId != 0L && it.productId == product.id) ||
                                    (it.productName.equals(product.name, ignoreCase = true) && it.size.equals(product.size, ignoreCase = true))
                                }
                                if (item != null) {
                                    viewModel.decrementCount(item)
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showEditOrderDialog) {
        EditOrderNumberDialog(
            currentOrderNumber = orderNumber,
            onDismiss = { showEditOrderDialog = false },
            onConfirm = { newNum ->
                viewModel.setOrderNumber(newNum)
                showEditOrderDialog = false
            }
        )
    }

    if (showOrderCalculatorDialog) {
        OrderCalculatorDialog(
            orderNumber = orderNumber,
            scanItems = scanItems,
            grandTotal = viewModel.grandTotal,
            totalPieces = viewModel.totalPieces,
            typesCount = viewModel.typesCount,
            tenderAmount = tenderAmount,
            changeDue = viewModel.changeDue,
            notes = notes,
            onNotesChanged = { viewModel.setNotes(it) },
            onTenderAmountChanged = { viewModel.setTenderAmount(it) },
            onIncrement = { viewModel.incrementCount(it) },
            onDecrement = { viewModel.decrementCount(it) },
            onRemove = { viewModel.removeLineItem(it) },
            onSave = {
                viewModel.saveCurrentScan {
                    showOrderCalculatorDialog = false
                }
            },
            onClear = { showClearConfirmDialog = true },
            onViewReceipt = { showReceiptDialog = true },
            onDismiss = { showOrderCalculatorDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = CacaoCard,
            title = {
                Text("Clear Order #$orderNumber?", color = GoldLight, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Remove all items from the current order tally?", color = Color(0xFFD7CCC8))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearOrder()
                        showClearConfirmDialog = false
                        showOrderCalculatorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB83A4B), contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_clear_order_button")
                ) {
                    Text("Clear Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = GoldLight)
                }
            }
        )
    }

    if (showReceiptDialog) {
        OrderReceiptDialog(
            orderNumber = orderNumber,
            scanItems = scanItems,
            grandTotal = viewModel.grandTotal,
            totalPieces = viewModel.totalPieces,
            tenderAmount = tenderAmount,
            changeDue = viewModel.changeDue,
            notes = notes,
            onDismiss = { showReceiptDialog = false }
        )
    }
}

@Composable
private fun OrderHeaderBar(
    orderNumber: String,
    grandTotal: Double,
    onEditOrderNumber: () -> Unit,
    onNewOrder: () -> Unit,
    onOpenCalculator: () -> Unit,
    hasItems: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CacaoDark,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CacaoCard)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onEditOrderNumber() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("order_number_badge")
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Edit order number",
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "#$orderNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFFA1887F),
                    modifier = Modifier.size(12.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onNewOrder,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("new_order_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (hasItems) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenCalculator,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("quick_view_order_button")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatLKR(grandTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_search_bar"),
            placeholder = {
                Text(
                    text = "Search products (e.g. 100g, slab, cake, mocha)...",
                    color = Color(0xFF9E8E81),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.testTag("clear_order_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFFD7CCC8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
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

@Composable
private fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    products: List<Product>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory.equals(category, ignoreCase = true)
            val count = if (category == "All") {
                products.size
            } else {
                products.count { it.category.equals(category, ignoreCase = true) }
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = "$category ($count)",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    if (category != "All") {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
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
                border = BorderStroke(
                    1.dp,
                    if (isSelected) GoldPrimary else CacaoBorder
                ),
                modifier = Modifier.testTag("category_chip_$category")
            )
        }
    }
}

@Composable
private fun ProductOrderRow(
    product: Product,
    currentCount: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_row_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (currentCount > 0) CacaoCard else CacaoDark
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (currentCount > 0) GoldPrimary.copy(alpha = 0.7f) else CacaoBorder.copy(alpha = 0.5f)
        )
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
                size = 46.dp
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatLKR(product.price),
                    color = GoldLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (currentCount == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CacaoBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("add_button_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    QuantityStepper(
                        quantity = currentCount,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                        testTagPrefix = "product_${product.id}"
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total: ${formatLKR(product.price * currentCount)}",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomOrderSummaryBar(
    orderNumber: String,
    totalPieces: Int,
    typesCount: Int,
    grandTotal: Double,
    onOpenCalculator: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_order_summary_bar"),
        color = CacaoCard,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Order #$orderNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$totalPieces pcs ($typesCount types)",
                        color = Color(0xFFC7B299),
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        color = Color(0xFFA1887F),
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatLKR(grandTotal),
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onOpenCalculator,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CacaoBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("open_order_calculator_button")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
