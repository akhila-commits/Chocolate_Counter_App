package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Product
import com.example.data.model.ScanItem
import com.example.data.model.ScanRecord
import com.example.data.remote.GeminiVisionService
import com.example.data.repository.ProductRepository
import com.example.data.repository.ScanRepository
import com.example.util.ImageStorageUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Analyzing(val statusMessage: String) : ScanUiState
    data class ResultReady(val summary: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val productRepository = ProductRepository(database.productDao())
    private val scanRepository = ScanRepository(database.scanRecordDao())
    private val visionService = GeminiVisionService()

    private val moshi = Moshi.Builder().build()
    private val scanItemListType = Types.newParameterizedType(List::class.java, ScanItem::class.java)
    private val scanItemAdapter = moshi.adapter<List<ScanItem>>(scanItemListType)

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _sampleTrayTitle = MutableStateFlow<String?>(null)
    val sampleTrayTitle: StateFlow<String?> = _sampleTrayTitle.asStateFlow()

    // Assortment classification for the tray tally
    private val _assortmentName = MutableStateFlow("12-Piece Deluxe Box")
    val assortmentName: StateFlow<String> = _assortmentName.asStateFlow()

    // Enhanced camera framing grid overlay
    private val _showGridOverlay = MutableStateFlow(true)
    val showGridOverlay: StateFlow<Boolean> = _showGridOverlay.asStateFlow()

    private val _scanItems = MutableStateFlow<List<ScanItem>>(emptyList())
    val scanItems: StateFlow<List<ScanItem>> = _scanItems.asStateFlow()

    private val _unrecognizedCount = MutableStateFlow(0)
    val unrecognizedCount: StateFlow<Int> = _unrecognizedCount.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Price math is always calculated in app code
    val grandTotal: Double
        get() = _scanItems.value.sumOf { it.count * it.unitPrice }

    val totalPieces: Int
        get() = _scanItems.value.sumOf { it.count }

    val typesCount: Int
        get() = _scanItems.value.count { it.count > 0 }

    fun setAssortmentName(name: String) {
        _assortmentName.value = name.trim().ifEmpty { "Custom Assortment" }
    }

    fun toggleGridOverlay() {
        _showGridOverlay.value = !_showGridOverlay.value
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
    }

    fun onImageSelected(bitmap: Bitmap, trayTitle: String? = null, defaultAssortment: String? = null) {
        _selectedBitmap.value = bitmap
        _sampleTrayTitle.value = trayTitle
        if (defaultAssortment != null) {
            _assortmentName.value = defaultAssortment
        }
        _uiState.value = ScanUiState.Idle
        _scanItems.value = emptyList()
        _unrecognizedCount.value = 0
        _notes.value = ""
    }

    fun startRecognition() {
        val bitmap = _selectedBitmap.value ?: return
        viewModelScope.launch {
            _uiState.value = ScanUiState.Analyzing("Connecting to Gemini Vision...")
            val catalog = productRepository.getProductList()
            if (catalog.isEmpty()) {
                _uiState.value = ScanUiState.Error("Product catalog is empty. Please add products first.")
                return@launch
            }

            _uiState.value = ScanUiState.Analyzing("Analyzing tray for ${_assortmentName.value}...")
            val result = visionService.recognizeChocolates(
                bitmap = bitmap,
                catalog = catalog,
                customApiKey = _customApiKey.value.takeIf { it.isNotEmpty() }
            )

            result.onSuccess { rec ->
                // Map detected items to current catalog snapshots
                val lineItems = mutableListOf<ScanItem>()
                rec.items.forEach { detected ->
                    val matchedProduct = catalog.find { it.name.equals(detected.productName, ignoreCase = true) }
                    if (matchedProduct != null && detected.count > 0) {
                        lineItems.add(
                            ScanItem(
                                productName = matchedProduct.name,
                                unitPrice = matchedProduct.price,
                                count = detected.count,
                                confidence = detected.confidence,
                                shape = matchedProduct.shape,
                                colorHex = matchedProduct.colorHex
                            )
                        )
                    }
                }

                _scanItems.value = lineItems
                _unrecognizedCount.value = rec.unrecognizedCount
                _notes.value = rec.notes
                val summary = generateSummaryText()
                _uiState.value = ScanUiState.ResultReady(summary)
            }.onFailure { err ->
                _uiState.value = ScanUiState.Error(err.localizedMessage ?: "Recognition failed. Please check connection.")
            }
        }
    }

    fun incrementCount(productName: String) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.productName == productName }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(count = old.count + 1)
            _scanItems.value = current
            updateSummaryState()
        }
    }

    fun decrementCount(productName: String) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.productName == productName }
        if (index != -1) {
            val old = current[index]
            if (old.count > 1) {
                current[index] = old.copy(count = old.count - 1)
            } else {
                current.removeAt(index)
            }
            _scanItems.value = current
            updateSummaryState()
        }
    }

    fun setCount(productName: String, newCount: Int) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.productName == productName }
        if (index != -1) {
            if (newCount > 0) {
                val old = current[index]
                current[index] = old.copy(count = newCount)
            } else {
                current.removeAt(index)
            }
            _scanItems.value = current
            updateSummaryState()
        }
    }

    fun assignUnrecognizedToProduct(product: Product, quantityToAssign: Int) {
        val qty = quantityToAssign.coerceIn(1, _unrecognizedCount.value)
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.productName.equals(product.name, ignoreCase = true) }

        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(count = old.count + qty)
        } else {
            current.add(
                ScanItem(
                    productName = product.name,
                    unitPrice = product.price,
                    count = qty,
                    confidence = 1.0,
                    shape = product.shape,
                    colorHex = product.colorHex
                )
            )
        }

        _scanItems.value = current
        _unrecognizedCount.value = (_unrecognizedCount.value - qty).coerceAtLeast(0)
        updateSummaryState()
    }

    fun addMissedProduct(product: Product) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.productName.equals(product.name, ignoreCase = true) }

        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(count = old.count + 1)
        } else {
            current.add(
                ScanItem(
                    productName = product.name,
                    unitPrice = product.price,
                    count = 1,
                    confidence = 1.0,
                    shape = product.shape,
                    colorHex = product.colorHex
                )
            )
        }
        _scanItems.value = current
        updateSummaryState()
    }

    fun removeLineItem(productName: String) {
        val current = _scanItems.value.toMutableList()
        current.removeAll { it.productName == productName }
        _scanItems.value = current
        updateSummaryState()
    }

    private fun updateSummaryState() {
        if (_uiState.value is ScanUiState.ResultReady) {
            _uiState.value = ScanUiState.ResultReady(generateSummaryText())
        }
    }

    fun generateSummaryText(): String {
        val pieces = totalPieces
        val types = typesCount
        val total = grandTotal
        val formatted = formatLKR(total)
        val assortment = _assortmentName.value
        return if (pieces > 0) {
            "[$assortment] $pieces piece${if (pieces == 1) "" else "s"} across $types type${if (types == 1) "" else "s"}. Total: $formatted"
        } else {
            "0 pieces counted. Total: LKR 0.00"
        }
    }

    fun saveCurrentScan(onSuccess: () -> Unit) {
        if (_scanItems.value.isEmpty()) return
        viewModelScope.launch {
            try {
                val bitmap = _selectedBitmap.value
                val savedPhotoPath = bitmap?.let {
                    ImageStorageUtil.saveBitmapToInternalStorage(getApplication(), it, "tray_scans")
                }

                val jsonString = scanItemAdapter.toJson(_scanItems.value)
                val summary = generateSummaryText()
                val record = ScanRecord(
                    assortmentName = _assortmentName.value,
                    photoUri = savedPhotoPath,
                    sampleTrayTitle = _sampleTrayTitle.value,
                    totalAmount = grandTotal,
                    totalPieces = totalPieces,
                    typesCount = typesCount,
                    summaryText = summary,
                    itemsJson = jsonString,
                    notes = _notes.value
                )
                scanRepository.saveScan(record)
                _toastEvent.emit("Tally saved for ${_assortmentName.value}")
                onSuccess()
            } catch (e: Exception) {
                _toastEvent.emit("Failed to save: ${e.message}")
            }
        }
    }

    fun resetScan() {
        _selectedBitmap.value = null
        _sampleTrayTitle.value = null
        _scanItems.value = emptyList()
        _unrecognizedCount.value = 0
        _notes.value = ""
        _uiState.value = ScanUiState.Idle
    }
}

fun formatLKR(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "LKR ${formatter.format(amount)}"
}
