package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
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

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(allProducts, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.shape.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addProduct(
        name: String,
        price: Double,
        description: String,
        shape: String,
        colorHex: String,
        referenceImageUri: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addProduct(
                name = name,
                price = price,
                description = description,
                shape = shape,
                colorHex = colorHex,
                referenceImageUri = referenceImageUri
            )
            result.onSuccess {
                _snackbarEvent.emit("Added \"$name\" to catalog")
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
                _snackbarEvent.emit("Updated \"${product.name}\"")
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
                _snackbarEvent.emit("Chocolate picture updated")
                onSuccess()
            }.onFailure { err ->
                _snackbarEvent.emit(err.message ?: "Failed to update image")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _snackbarEvent.emit("Deleted \"${product.name}\" from catalog")
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
            _snackbarEvent.emit("Reset catalog to Revello signature menu")
        }
    }
}
