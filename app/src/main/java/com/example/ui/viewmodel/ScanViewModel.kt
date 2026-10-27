package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Product
import com.example.data.model.ScanItem
import com.example.data.model.ScanRecord
import com.example.data.repository.ProductRepository
import com.example.data.repository.ScanRepository
import com.example.ui.components.formatLKR
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

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val productRepository = ProductRepository(database.productDao())
    private val scanRepository = ScanRepository(database.scanRecordDao())

    private val moshi = Moshi.Builder().build()
    private val scanItemListType = Types.newParameterizedType(List::class.java, ScanItem::class.java)
    private val scanItemAdapter = moshi.adapter<List<ScanItem>>(scanItemListType)

    // Order reference number
    private val _orderNumber = MutableStateFlow(generateDefaultOrderNumber())
    val orderNumber: StateFlow<String> = _orderNumber.asStateFlow()

    // Search and category filter for fast ordering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Current order line items
    private val _scanItems = MutableStateFlow<List<ScanItem>>(emptyList())
    val scanItems: StateFlow<List<ScanItem>> = _scanItems.asStateFlow()

    // Cash Tender for Change Due calculation
    private val _tenderAmount = MutableStateFlow<Double?>(null)
    val tenderAmount: StateFlow<Double?> = _tenderAmount.asStateFlow()

    // Customer or order notes
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Math calculations performed strictly in app code
    val grandTotal: Double
        get() = _scanItems.value.sumOf { it.count * it.unitPrice }

    val totalPieces: Int
        get() = _scanItems.value.sumOf { it.count }

    val typesCount: Int
        get() = _scanItems.value.count { it.count > 0 }

    val changeDue: Double
        get() {
            val tender = _tenderAmount.value ?: return 0.0
            return (tender - grandTotal).coerceAtLeast(0.0)
        }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category ?: "All"
    }

    fun setOrderNumber(number: String) {
        val trimmed = number.trim()
        _orderNumber.value = if (trimmed.isEmpty()) generateDefaultOrderNumber() else trimmed
    }

    fun generateNewOrderNumber() {
        _orderNumber.value = generateDefaultOrderNumber()
    }

    private fun generateDefaultOrderNumber(): String {
        val randomDigits = (1000..9999).random()
        return "ORD-$randomDigits"
    }

    fun setTenderAmount(amount: Double?) {
        _tenderAmount.value = amount
    }

    fun setNotes(newNotes: String) {
        _notes.value = newNotes
    }

    /**
     * Add product to current order.
     * Identifies items by product id or matching name AND size,
     * so products with the same name but different grammes (e.g. 100g vs 250g)
     * are treated as distinct line items!
     */
    fun addProductToOrder(product: Product, quantity: Int = 1) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst {
            (it.productId != 0L && it.productId == product.id) ||
            (it.productName.equals(product.name, ignoreCase = true) && it.size.equals(product.size, ignoreCase = true))
        }

        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(count = existing.count + quantity)
        } else {
            current.add(
                ScanItem(
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    size = product.size,
                    unitPrice = product.price,
                    count = quantity,
                    confidence = 1.0,
                    shape = product.shape,
                    colorHex = product.colorHex
                )
            )
        }

        _scanItems.value = current
        viewModelScope.launch {
            _toastEvent.emit("Added ${product.displayTitle} to #${_orderNumber.value}")
        }
    }

    fun getItemCountForProduct(product: Product): Int {
        return _scanItems.value.firstOrNull {
            (it.productId != 0L && it.productId == product.id) ||
            (it.productName.equals(product.name, ignoreCase = true) && it.size.equals(product.size, ignoreCase = true))
        }?.count ?: 0
    }

    fun incrementCount(item: ScanItem) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.itemKey == item.itemKey }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(count = old.count + 1)
            _scanItems.value = current
        }
    }

    fun decrementCount(item: ScanItem) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.itemKey == item.itemKey }
        if (index != -1) {
            val old = current[index]
            if (old.count > 1) {
                current[index] = old.copy(count = old.count - 1)
                _scanItems.value = current
            } else {
                current.removeAt(index)
                _scanItems.value = current
            }
        }
    }

    fun setItemQuantity(item: ScanItem, quantity: Int) {
        val current = _scanItems.value.toMutableList()
        val index = current.indexOfFirst { it.itemKey == item.itemKey }
        if (index != -1) {
            if (quantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(count = quantity)
            }
            _scanItems.value = current
        }
    }

    fun removeLineItem(item: ScanItem) {
        val current = _scanItems.value.toMutableList()
        current.removeAll { it.itemKey == item.itemKey }
        _scanItems.value = current
    }

    fun clearOrder() {
        _scanItems.value = emptyList()
        _tenderAmount.value = null
        _notes.value = ""
    }

    fun resetScan() {
        clearOrder()
    }

    fun generateSummaryText(): String {
        val total = grandTotal
        val count = totalPieces
        val types = typesCount
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$count items ($types types) for Order #${_orderNumber.value}. Total: LKR ${formatter.format(total)}"
    }

    fun saveCurrentScan(onSaved: () -> Unit) {
        val itemsToSave = _scanItems.value.filter { it.count > 0 }
        if (itemsToSave.isEmpty()) {
            viewModelScope.launch {
                _toastEvent.emit("Cannot save an empty order. Please add products.")
            }
            return
        }

        val total = grandTotal
        val pieces = totalPieces
        val types = typesCount
        val summary = generateSummaryText()
        val json = runCatching { scanItemAdapter.toJson(itemsToSave) }.getOrDefault("[]")
        val savedOrderNumber = _orderNumber.value

        viewModelScope.launch {
            val record = ScanRecord(
                orderNumber = savedOrderNumber,
                photoUri = null,
                sampleTrayTitle = "Counter Order",
                totalAmount = total,
                totalPieces = pieces,
                typesCount = types,
                summaryText = summary,
                itemsJson = json,
                notes = _notes.value
            )
            scanRepository.saveScan(record)
            _toastEvent.emit("Order #$savedOrderNumber saved successfully (${formatLKR(total)})")

            // Prepare next order
            clearOrder()
            generateNewOrderNumber()
            onSaved()
        }
    }

    fun loadSavedOrder(record: ScanRecord) {
        val items = runCatching {
            scanItemAdapter.fromJson(record.itemsJson)
        }.getOrNull() ?: emptyList()

        _orderNumber.value = record.orderNumber.ifBlank { generateDefaultOrderNumber() }
        _scanItems.value = items
        _notes.value = record.notes
        _tenderAmount.value = null

        viewModelScope.launch {
            _toastEvent.emit("Loaded Order #${_orderNumber.value} into calculator")
        }
    }
}
