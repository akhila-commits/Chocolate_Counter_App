package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PRODUCT_CATEGORIES
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import com.example.ui.components.formatLKR
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ProductRepository(database.productDao())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        var result = list

        if (!category.isNullOrBlank() && category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim()
            result = result.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.size.contains(q, ignoreCase = true) ||
                it.category.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category ?: "All"
    }

    fun addProduct(
        name: String,
        category: String,
        size: String,
        price: Double,
        description: String,
        shape: String = "Piece",
        colorHex: String = "#3B1E15",
        referenceImageUri: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addProduct(
                name = name,
                category = category,
                size = size,
                price = price,
                description = description,
                shape = shape,
                colorHex = colorHex,
                referenceImageUri = referenceImageUri
            )
            result.onSuccess {
                val label = if (size.isNotBlank()) "$name ($size)" else name
                _snackbarEvent.emit("Added \"$label\" to catalog")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to add product")
            }
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProduct(product)
            result.onSuccess {
                val label = if (product.size.isNotBlank()) "${product.name} (${product.size})" else product.name
                _snackbarEvent.emit("Updated \"$label\"")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to update product")
            }
        }
    }

    fun updatePrice(productId: Long, newPrice: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.updatePrice(productId, newPrice)
            result.onSuccess {
                _snackbarEvent.emit("Price updated to ${formatLKR(newPrice)}")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to update price")
            }
        }
    }

    fun updateImage(productId: Long, imageUri: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.updateImage(productId, imageUri)
            result.onSuccess {
                _snackbarEvent.emit("Product picture updated")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to update image")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _snackbarEvent.emit("Deleted \"${product.displayTitle}\" from catalog")
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
            _snackbarEvent.emit("Reset catalog to signature shop menu")
        }
    }
}
