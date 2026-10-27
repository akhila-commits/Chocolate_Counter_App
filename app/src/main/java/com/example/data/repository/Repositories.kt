package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ProductDao
import com.example.data.local.ScanRecordDao
import com.example.data.model.Product
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductList(): List<Product> = productDao.getProductList()

    suspend fun addProduct(
        name: String,
        category: String = "Chocolate pieces",
        size: String = "",
        price: Double,
        description: String = "",
        shape: String = "Piece",
        colorHex: String = "#3B1E15",
        referenceImageUri: String? = null
    ): Result<Long> {
        val trimmedName = name.trim()
        val trimmedSize = size.trim()
        val trimmedCategory = category.trim().ifEmpty { "Chocolate pieces" }

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Product name cannot be empty"))
        }
        if (price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be a positive number (LKR)"))
        }

        // Allow products with the same name if their size/grammes differs
        val existing = productDao.getProductByNameAndSize(trimmedName, trimmedSize)
        if (existing != null) {
            val label = if (trimmedSize.isNotEmpty()) "'$trimmedName ($trimmedSize)'" else "'$trimmedName'"
            return Result.failure(IllegalArgumentException("A product with name and size $label already exists"))
        }

        val product = Product(
            name = trimmedName,
            category = trimmedCategory,
            size = trimmedSize,
            price = price,
            description = description.trim(),
            shape = shape,
            colorHex = colorHex,
            referenceImageUri = referenceImageUri,
            isDefault = false
        )
        val id = productDao.insertProduct(product)
        return Result.success(id)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        val trimmedName = product.name.trim()
        val trimmedSize = product.size.trim()
        val trimmedCategory = product.category.trim().ifEmpty { "Chocolate pieces" }

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Product name cannot be empty"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be a positive number (LKR)"))
        }

        val existing = productDao.getProductByNameAndSize(trimmedName, trimmedSize)
        if (existing != null && existing.id != product.id) {
            val label = if (trimmedSize.isNotEmpty()) "'$trimmedName ($trimmedSize)'" else "'$trimmedName'"
            return Result.failure(IllegalArgumentException("Another product with $label already exists"))
        }

        productDao.updateProduct(
            product.copy(
                name = trimmedName,
                size = trimmedSize,
                category = trimmedCategory
            )
        )
        return Result.success(Unit)
    }

    suspend fun updatePrice(productId: Long, newPrice: Double): Result<Unit> {
        if (newPrice <= 0) {
            return Result.failure(IllegalArgumentException("Price must be positive"))
        }
        productDao.updateProductPrice(productId, newPrice)
        return Result.success(Unit)
    }

    suspend fun updateImage(productId: Long, imageUri: String?): Result<Unit> {
        productDao.updateProductImage(productId, imageUri)
        return Result.success(Unit)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun resetToDefaults() {
        productDao.deleteAll()
        AppDatabase.populateInitialRevelloCatalog(productDao)
    }
}

class ScanRepository(private val scanRecordDao: ScanRecordDao) {
    val allScans: Flow<List<ScanRecord>> = scanRecordDao.getAllScanRecords()

    fun searchByOrderNumber(query: String): Flow<List<ScanRecord>> {
        return scanRecordDao.searchScanRecordsByOrderNumber(query)
    }

    suspend fun getScanByOrderNumber(orderNumber: String): ScanRecord? {
        return scanRecordDao.getScanRecordByOrderNumberSingle(orderNumber)
    }

    suspend fun saveOrUpdateScan(record: ScanRecord): Long {
        val existing = if (record.orderNumber.isNotBlank()) {
            scanRecordDao.getScanRecordByOrderNumberSingle(record.orderNumber)
        } else null
        return if (existing != null && record.id == 0L) {
            val updated = record.copy(id = existing.id)
            scanRecordDao.insertScanRecord(updated)
        } else {
            scanRecordDao.insertScanRecord(record)
        }
    }

    suspend fun saveScan(record: ScanRecord): Long {
        return saveOrUpdateScan(record)
    }

    suspend fun deleteScan(record: ScanRecord) {
        scanRecordDao.deleteScanRecord(record)
    }

    suspend fun getScanById(id: Long): ScanRecord? {
        return scanRecordDao.getScanRecordById(id)
    }
}
