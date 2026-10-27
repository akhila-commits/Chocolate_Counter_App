package com.example.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.ScanItem
import com.example.ui.components.ChocolateThumbnail
import com.example.ui.components.RevelloBrandBanner
import com.example.ui.components.SampleTrayPreset
import com.example.ui.components.SampleTrayPresets
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
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.ScanViewModel
import com.example.ui.viewmodel.formatLKR
import com.example.util.ImageStorageUtil
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    catalogProducts: List<Product>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedBitmap by viewModel.selectedBitmap.collectAsStateWithLifecycle()
    val sampleTrayTitle by viewModel.sampleTrayTitle.collectAsStateWithLifecycle()
    val assortmentName by viewModel.assortmentName.collectAsStateWithLifecycle()
    val showGridOverlay by viewModel.showGridOverlay.collectAsStateWithLifecycle()
    val scanItems by viewModel.scanItems.collectAsStateWithLifecycle()
    val unrecognizedCount by viewModel.unrecognizedCount.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showAddMissedDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showCustomAssortmentDialog by remember { mutableStateOf(false) }
    var editingItemForDirectCount by remember { mutableStateOf<ScanItem?>(null) }

    // State for high-res camera capture
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val bitmap = BitmapFactory.decodeFile(tempCameraFile!!.absolutePath)
            if (bitmap != null) {
                viewModel.onImageSelected(bitmap, "High-Res Counter Camera", assortmentName)
            }
        }
    }

    // Quick camera preview fallback
    val previewCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onImageSelected(bitmap, "Counter Snapshot", assortmentName)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.onImageSelected(bitmap, "Tray Photo Upload", assortmentName)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    var showCameraPermissionDeniedDialog by remember { mutableStateOf(false) }

    val launchCameraSafely = {
        try {
            val (uri, file) = ImageStorageUtil.createTempPictureUri(context)
            tempCameraUri = uri
            tempCameraFile = file
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            try {
                previewCameraLauncher.launch(null)
            } catch (err: Exception) {
                showCameraPermissionDeniedDialog = true
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraSafely()
        } else {
            showCameraPermissionDeniedDialog = true
        }
    }

    val requestCameraOrLaunch = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraSafely()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ScaffoldScanContent(
        modifier = modifier,
        uiState = uiState,
        selectedBitmap = selectedBitmap,
        sampleTrayTitle = sampleTrayTitle,
        assortmentName = assortmentName,
        showGridOverlay = showGridOverlay,
        scanItems = scanItems,
        catalogProducts = catalogProducts,
        unrecognizedCount = unrecognizedCount,
        notes = notes,
        grandTotal = viewModel.grandTotal,
        totalPieces = viewModel.totalPieces,
        typesCount = viewModel.typesCount,
        summaryText = viewModel.generateSummaryText(),
        onCameraClick = requestCameraOrLaunch,
        onUploadClick = {
            galleryLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        onSampleTraySelect = { preset ->
            val bitmap = SampleTrayBitmapGenerator.generateTrayBitmap(preset.id)
            viewModel.onImageSelected(bitmap, preset.title, preset.title)
        },
        onAssortmentSelected = { viewModel.setAssortmentName(it) },
        onCustomAssortmentClick = { showCustomAssortmentDialog = true },
        onToggleGrid = { viewModel.toggleGridOverlay() },
        onCountChocolatesClick = { viewModel.startRecognition() },
        onRetakeClick = { viewModel.resetScan() },
        onIncrement = { viewModel.incrementCount(it) },
        onDecrement = { viewModel.decrementCount(it) },
        onDirectCountClick = { editingItemForDirectCount = it },
        onRemoveItem = { viewModel.removeLineItem(it) },
        onAddMissedClick = { showAddMissedDialog = true },
        onAssignUnrecognizedClick = { showAssignDialog = true },
        onSaveScanClick = {
            viewModel.saveCurrentScan {
                viewModel.resetScan()
            }
        },
        onApiKeyClick = { showApiKeyDialog = true }
    )

    // Dialog: Custom Assortment Name
    if (showCustomAssortmentDialog) {
        var customInput by remember { mutableStateOf(assortmentName) }
        AlertDialog(
            onDismissRequest = { showCustomAssortmentDialog = false },
            title = {
                Text("Assortment Name", fontFamily = PlayfairFontFamily, color = GoldLight)
            },
            text = {
                Column {
                    Text(
                        "Enter the box or tray assortment type being tallied (e.g., 24-Piece Party Box, Kalutara Special):",
                        color = Color(0xFFD7CCC8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_assortment_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAssortmentName(customInput)
                        showCustomAssortmentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAssortmentDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Dialog: Camera Permission Denied / Rationale
    if (showCameraPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDeniedDialog = false },
            title = {
                Text("Camera Permission Needed", fontFamily = PlayfairFontFamily, color = GoldLight)
            },
            text = {
                Text(
                    "Camera access is required to take photos of chocolate trays directly. You can grant camera permission, or upload a photo from your gallery.",
                    color = Color(0xFFD7CCC8),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraPermissionDeniedDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionDeniedDialog = false }) {
                    Text("Dismiss", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Dialog: Direct count input
    editingItemForDirectCount?.let { item ->
        var countInput by remember { mutableStateOf(item.count.toString()) }
        AlertDialog(
            onDismissRequest = { editingItemForDirectCount = null },
            title = {
                Text(
                    text = "Edit Count: ${item.productName}",
                    fontFamily = PlayfairFontFamily,
                    color = GoldLight
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter exact number of pieces on tray:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD7CCC8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = countInput,
                        onValueChange = { countInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("direct_count_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCount = countInput.toIntOrNull() ?: item.count
                        viewModel.setCount(item.productName, newCount)
                        editingItemForDirectCount = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItemForDirectCount = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Dialog: Add missed chocolate
    if (showAddMissedDialog) {
        AlertDialog(
            onDismissRequest = { showAddMissedDialog = false },
            title = {
                Text(
                    text = "Add Missed Chocolate",
                    fontFamily = PlayfairFontFamily,
                    color = GoldLight
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(catalogProducts) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addMissedProduct(product)
                                    showAddMissedDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChocolateThumbnail(
                                shape = product.shape,
                                colorHex = product.colorHex,
                                imageUri = product.referenceImageUri,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = formatLKR(product.price),
                                    color = GoldLight,
                                    fontSize = 13.sp
                                )
                            }
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldPrimary)
                        }
                        HorizontalDivider(color = CacaoBorder.copy(alpha = 0.5f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMissedDialog = false }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Dialog: Assign unrecognized pieces
    if (showAssignDialog && unrecognizedCount > 0) {
        var selectedProduct by remember { mutableStateOf<Product?>(catalogProducts.firstOrNull()) }
        var quantityToAssign by remember { mutableStateOf(1) }

        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = {
                Text(
                    text = "Assign Unrecognized Items",
                    fontFamily = PlayfairFontFamily,
                    color = GoldLight
                )
            },
            text = {
                Column {
                    Text(
                        text = "You have $unrecognizedCount unrecognized piece(s). Choose which catalog product to assign them to:",
                        color = Color(0xFFD7CCC8),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(catalogProducts) { product ->
                            val isSelected = selectedProduct?.id == product.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CacaoCard else Color.Transparent)
                                    .clickable { selectedProduct = product }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChocolateThumbnail(
                                    shape = product.shape,
                                    colorHex = product.colorHex,
                                    imageUri = product.referenceImageUri,
                                    size = 34.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, color = Color.White, fontSize = 14.sp)
                                    Text(formatLKR(product.price), color = GoldLight, fontSize = 12.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedProduct?.let { prod ->
                            viewModel.assignUnrecognizedToProduct(prod, quantityToAssign)
                        }
                        showAssignDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Assign as Product", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Dialog: Gemini API key configuration
    if (showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(customApiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text("Gemini API Key Configuration", fontFamily = PlayfairFontFamily, color = GoldLight)
            },
            text = {
                Column {
                    Text(
                        "The app automatically uses the server-injected Gemini API key or sample recognition. You can also provide a custom Gemini API key:",
                        color = Color(0xFFD7CCC8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("AIzaSy...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomApiKey(keyInput)
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }
}

@Composable
private fun ScaffoldScanContent(
    modifier: Modifier,
    uiState: ScanUiState,
    selectedBitmap: Bitmap?,
    sampleTrayTitle: String?,
    assortmentName: String,
    showGridOverlay: Boolean,
    scanItems: List<ScanItem>,
    catalogProducts: List<Product>,
    unrecognizedCount: Int,
    notes: String,
    grandTotal: Double,
    totalPieces: Int,
    typesCount: Int,
    summaryText: String,
    onCameraClick: () -> Unit,
    onUploadClick: () -> Unit,
    onSampleTraySelect: (SampleTrayPreset) -> Unit,
    onAssortmentSelected: (String) -> Unit,
    onCustomAssortmentClick: () -> Unit,
    onToggleGrid: () -> Unit,
    onCountChocolatesClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onDirectCountClick: (ScanItem) -> Unit,
    onRemoveItem: (String) -> Unit,
    onAddMissedClick: () -> Unit,
    onAssignUnrecognizedClick: () -> Unit,
    onSaveScanClick: () -> Unit,
    onApiKeyClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CacaoBlack),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            RevelloBrandBanner()
        }

        // Header Action Bar: Title + Key Settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tray Tally & AI Counter",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = "Assortment: $assortmentName",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onApiKeyClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("api_key_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key Settings",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Assortment Selection Strip
        item {
            AssortmentSelectorStrip(
                currentAssortment = assortmentName,
                onSelect = onAssortmentSelected,
                onCustomClick = onCustomAssortmentClick
            )
        }

        // Photo Input Actions or Tray Preview
        if (selectedBitmap == null) {
            item {
                CaptureOptionsCard(
                    onCameraClick = onCameraClick,
                    onUploadClick = onUploadClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                SampleTraySelectorSection(onSelect = onSampleTraySelect)
            }
        } else {
            // Preview card with enhanced grid framing overlay
            item {
                TrayPreviewCard(
                    bitmap = selectedBitmap,
                    trayTitle = sampleTrayTitle,
                    assortmentName = assortmentName,
                    showGridOverlay = showGridOverlay,
                    onToggleGrid = onToggleGrid,
                    isAnalyzing = uiState is ScanUiState.Analyzing,
                    isResultReady = uiState is ScanUiState.ResultReady,
                    onCountClick = onCountChocolatesClick,
                    onRetakeClick = onRetakeClick
                )
            }
        }

        // Loading State during Gemini API call
        if (uiState is ScanUiState.Analyzing) {
            item {
                AnalyzingProgressCard(statusMessage = (uiState as ScanUiState.Analyzing).statusMessage)
            }
        }

        // Error State
        if (uiState is ScanUiState.Error) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381414)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RubyBerryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recognition Notice", fontWeight = FontWeight.Bold, color = Color.White)
                            Text((uiState as ScanUiState.Error).message, color = Color(0xFFFFCDD2), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Recognition Results & Line Items Table
        if (uiState is ScanUiState.ResultReady || (selectedBitmap != null && scanItems.isNotEmpty())) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Assortment Tally Results ($assortmentName)",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = "Verify and correct piece counts before saving",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC7B299)
                    )
                }
            }

            // Unrecognized alert if count > 0
            if (unrecognizedCount > 0) {
                item {
                    UnrecognizedAlertCard(
                        count = unrecognizedCount,
                        onAssignClick = onAssignUnrecognizedClick
                    )
                }
            }

            // Line items
            items(scanItems) { item ->
                val matchedProduct = catalogProducts.find { it.name.equals(item.productName, ignoreCase = true) }
                ScanItemRow(
                    item = item,
                    imageUri = matchedProduct?.referenceImageUri,
                    onIncrement = { onIncrement(item.productName) },
                    onDecrement = { onDecrement(item.productName) },
                    onDirectCountClick = { onDirectCountClick(item) },
                    onRemove = { onRemoveItem(item.productName) }
                )
            }

            // Add missed item action
            item {
                OutlinedButton(
                    onClick = onAddMissedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("add_missed_chocolate_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+ Add Chocolate Staff Noticed (AI Missed)", fontWeight = FontWeight.SemiBold)
                }
            }

            // Summary notes from AI (if any)
            if (notes.isNotBlank()) {
                item {
                    Text(
                        text = "AI Observation: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1887F),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // Grand Total Card (prominent LKR display)
            item {
                GrandTotalCard(
                    grandTotal = grandTotal,
                    totalPieces = totalPieces,
                    typesCount = typesCount,
                    summaryText = summaryText,
                    onSaveClick = onSaveScanClick,
                    onDiscardClick = onRetakeClick
                )
            }
        }
    }
}

@Composable
private fun AssortmentSelectorStrip(
    currentAssortment: String,
    onSelect: (String) -> Unit,
    onCustomClick: () -> Unit
) {
    val commonAssortments = listOf(
        "12-Piece Deluxe Box",
        "8-Piece Praline Box",
        "16-Piece Platter",
        "Assorted Gift Box",
        "Custom Tray"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "Target Assortment:",
            color = Color(0xFFD7CCC8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonAssortments.forEach { name ->
                val isSelected = currentAssortment == name
                Surface(
                    color = if (isSelected) GoldPrimary else CacaoDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) GoldLight else CacaoBorder),
                    modifier = Modifier.clickable { onSelect(name) }
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) CacaoBlack else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            // Custom button
            Surface(
                color = CacaoSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldDark),
                modifier = Modifier.clickable(onClick = onCustomClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = GoldLight, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom...", color = GoldLight, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CaptureOptionsCard(
    onCameraClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CacaoBorder, GoldDark.copy(alpha = 0.5f))))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GoldPrimary, CacaoDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = CacaoBlack,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Capture Box or Tray",
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = GoldLight
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Camera Guidance Banner
            Surface(
                color = CacaoBlack.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hold camera 30cm directly above the tray. Keep in single layer.",
                        color = Color(0xFFD7CCC8),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("camera_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CacaoBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUploadClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("upload_photo_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldLight
                    ),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldPrimary, GoldDark))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SampleTraySelectorSection(
    onSelect: (SampleTrayPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Test with Sample Trays",
                fontFamily = PlayfairFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = CacaoCard,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Demo Mode",
                    color = GoldPrimary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SampleTrayPresets.forEach { preset ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable { onSelect(preset) }
                        .testTag("sample_tray_${preset.id}"),
                    colors = CardDefaults.cardColors(containerColor = CacaoDark),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CacaoBorder, GoldDark.copy(alpha = 0.4f))))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(preset.previewColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${preset.pieceCount} Pieces",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            color = Color(0xFFD7CCC8),
                            fontSize = 11.sp,
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrayPreviewCard(
    bitmap: Bitmap,
    trayTitle: String?,
    assortmentName: String,
    showGridOverlay: Boolean,
    onToggleGrid: () -> Unit,
    isAnalyzing: Boolean,
    isResultReady: Boolean,
    onCountClick: () -> Unit,
    onRetakeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CacaoBorder, GoldPrimary.copy(alpha = 0.5f))))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trayTitle ?: "Captured Tray",
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldLight
                    )
                    Text(
                        text = "Assortment: $assortmentName",
                        color = Color(0xFFD7CCC8),
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleGrid,
                        modifier = Modifier.testTag("toggle_camera_grid_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle Grid Alignment",
                            tint = if (showGridOverlay) GoldPrimary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    TextButton(
                        onClick = onRetakeClick,
                        enabled = !isAnalyzing,
                        modifier = Modifier.testTag("retake_photo_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", color = GoldPrimary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image container with optional Framing Grid Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CacaoBorder, RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Tray Preview",
                    modifier = Modifier.fillMaxSize()
                )

                // Golden 3x3 framing grid alignment lines
                if (showGridOverlay) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Rule of thirds lines
                        val gridColor = Color(0x77FFD54F)
                        drawLine(color = gridColor, start = Offset(w / 3f, 0f), end = Offset(w / 3f, h), strokeWidth = 1.5f)
                        drawLine(color = gridColor, start = Offset(2f * w / 3f, 0f), end = Offset(2f * w / 3f, h), strokeWidth = 1.5f)
                        drawLine(color = gridColor, start = Offset(0f, h / 3f), end = Offset(w, h / 3f), strokeWidth = 1.5f)
                        drawLine(color = gridColor, start = Offset(0f, 2f * h / 3f), end = Offset(w, 2f * h / 3f), strokeWidth = 1.5f)

                        // Center framing bracket
                        val bracketColor = Color(0xAAFFD54F)
                        val inset = 24f
                        val bLen = 30f
                        // Top-left
                        drawLine(color = bracketColor, start = Offset(inset, inset), end = Offset(inset + bLen, inset), strokeWidth = 3f)
                        drawLine(color = bracketColor, start = Offset(inset, inset), end = Offset(inset, inset + bLen), strokeWidth = 3f)
                        // Top-right
                        drawLine(color = bracketColor, start = Offset(w - inset, inset), end = Offset(w - inset - bLen, inset), strokeWidth = 3f)
                        drawLine(color = bracketColor, start = Offset(w - inset, inset), end = Offset(w - inset, inset + bLen), strokeWidth = 3f)
                        // Bottom-left
                        drawLine(color = bracketColor, start = Offset(inset, h - inset), end = Offset(inset + bLen, h - inset), strokeWidth = 3f)
                        drawLine(color = bracketColor, start = Offset(inset, h - inset), end = Offset(inset, h - inset - bLen), strokeWidth = 3f)
                        // Bottom-right
                        drawLine(color = bracketColor, start = Offset(w - inset, h - inset), end = Offset(w - inset - bLen, h - inset), strokeWidth = 3f)
                        drawLine(color = bracketColor, start = Offset(w - inset, h - inset), end = Offset(w - inset, h - inset - bLen), strokeWidth = 3f)
                    }
                }
            }

            if (!isResultReady && !isAnalyzing) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onCountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("count_chocolates_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CacaoBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Identify & Count ($assortmentName)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyzingProgressCard(statusMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GoldPrimary, CacaoDark))),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = GoldLight,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Counting Artisanal Chocolates...",
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GoldLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD7CCC8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UnrecognizedAlertCard(
    count: Int,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(AmberCaramel, GoldPrimary)))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberCaramel)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$count Unrecognized Piece${if (count == 1) "" else "s"}",
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Partially hidden or not in catalog",
                        color = Color(0xFFFFCC80),
                        fontSize = 12.sp
                    )
                }
            }
            Button(
                onClick = onAssignClick,
                colors = ButtonDefaults.buttonColors(containerColor = AmberCaramel, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("assign_unrecognized_button")
            ) {
                Text("Assign as...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScanItemRow(
    item: ScanItem,
    imageUri: String?,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDirectCountClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CacaoBorder, CacaoCard)))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChocolateThumbnail(
                shape = item.shape,
                colorHex = item.colorHex,
                imageUri = imageUri,
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${formatLKR(item.unitPrice)} each",
                    color = Color(0xFFD7CCC8),
                    fontSize = 12.sp
                )
            }

            // Editable Quantity with large touch targets (+ / -)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(CacaoSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, CacaoBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("decrement_${item.productName}")
                ) {
                    Icon(
                        imageVector = if (item.count > 1) Icons.Default.Remove else Icons.Default.DeleteOutline,
                        contentDescription = "Decrease count",
                        tint = if (item.count > 1) GoldLight else RubyBerryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tap count to enter number directly
                Text(
                    text = "${item.count}",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable(onClick = onDirectCountClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("count_display_${item.productName}")
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("increment_${item.productName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase count",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Calculated line total (computed by app code)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatLKR(item.lineTotal),
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GrandTotalCard(
    grandTotal: Double,
    totalPieces: Int,
    typesCount: Int,
    summaryText: String,
    onSaveClick: () -> Unit,
    onDiscardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CacaoCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldDark, GoldLight)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND TOTAL",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFFD7CCC8),
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = CacaoSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CacaoBorder)
                ) {
                    Text(
                        text = "$totalPieces pieces • $typesCount types",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Grand Total in LKR
            Text(
                text = formatLKR(grandTotal),
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = GoldPrimary,
                modifier = Modifier.testTag("grand_total_text")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Auto-generated human readable summary line
            Surface(
                color = CacaoBlack.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Save / Discard Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscardClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("discard_scan_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFCDD2)),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(RubyBerryColor, CacaoBorder))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Discard", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(50.dp)
                        .testTag("save_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CacaoBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to History", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
