package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ScanItem
import com.example.data.model.ScanRecord
import com.example.data.repository.ScanRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ScanRepository(database.scanRecordDao())

    private val moshi = Moshi.Builder().build()
    private val scanItemListType = Types.newParameterizedType(List::class.java, ScanItem::class.java)
    private val scanItemAdapter = moshi.adapter<List<ScanItem>>(scanItemListType)

    val allScans: StateFlow<List<ScanRecord>> = repository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredScans: StateFlow<List<ScanRecord>> = combine(allScans, _searchQuery) { scans, query ->
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            scans
        } else {
            scans.filter { 
                it.orderNumber.contains(trimmed, ignoreCase = true) ||
                it.summaryText.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteScan(record: ScanRecord) {
        viewModelScope.launch {
            repository.deleteScan(record)
            _snackbarEvent.emit("Order #${record.orderNumber} deleted from history")
        }
    }

    fun parseScanItems(jsonStr: String): List<ScanItem> {
        return try {
            scanItemAdapter.fromJson(jsonStr).orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
