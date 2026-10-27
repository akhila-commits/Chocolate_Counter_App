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
        price: Double,
        description: String,
        shape: String,
        colorHex: String,
        referenceImageUri: String? = null
    ): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Product name cannot be empty"))
        }
        if (price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be a positive number (LKR)"))
        }
        val existing = productDao.getProductByName(trimmed)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("A chocolate with name '$trimmed' already exists"))
        }

        val product = Product(
            name = trimmed,
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
        val trimmed = product.name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Product name cannot be empty"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be a positive number (LKR)"))
        }
        val existing = productDao.getProductByName(trimmed)
        if (existing != null && existing.id != product.id) {
            return Result.failure(IllegalArgumentException("Another chocolate with name '$trimmed' already exists"))
        }
        productDao.updateProduct(product.copy(name = trimmed))
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
    val distinctAssortments: Flow<List<String>> = scanRecordDao.getDistinctAssortments()

    fun getScansByAssortment(assortment: String): Flow<List<ScanRecord>> {
        return scanRecordDao.getScanRecordsByAssortment(assortment)
    }

    suspend fun saveScan(record: ScanRecord): Long {
        return scanRecordDao.insertScanRecord(record)
    }

    suspend fun deleteScan(record: ScanRecord) {
        scanRecordDao.deleteScanRecord(record)
    }

    suspend fun getScanById(id: Long): ScanRecord? {
        return scanRecordDao.getScanRecordById(id)
    }
}
