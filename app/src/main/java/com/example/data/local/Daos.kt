package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC, size ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY category ASC, name ASC, size ASC")
    suspend fun getProductList(): List<Product>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC, size ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE LOWER(name) = LOWER(:name) AND LOWER(size) = LOWER(:size) LIMIT 1")
    suspend fun getProductByNameAndSize(name: String, size: String): Product?

    @Query("SELECT * FROM products WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getProductByName(name: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET price = :newPrice WHERE id = :id")
    suspend fun updateProductPrice(id: Long, newPrice: Double)

    @Query("UPDATE products SET referenceImageUri = :imageUri WHERE id = :id")
    suspend fun updateProductImage(id: Long, imageUri: String?)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

@Dao
interface ScanRecordDao {
    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    fun getAllScanRecords(): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE LOWER(orderNumber) = LOWER(:orderNumber) ORDER BY timestamp DESC")
    fun getScanRecordsByOrderNumber(orderNumber: String): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE LOWER(orderNumber) LIKE '%' || LOWER(:query) || '%' ORDER BY timestamp DESC")
    fun searchScanRecordsByOrderNumber(query: String): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE LOWER(orderNumber) = LOWER(:orderNumber) LIMIT 1")
    suspend fun getScanRecordByOrderNumberSingle(orderNumber: String): ScanRecord?

    @Query("SELECT * FROM scan_records WHERE id = :id LIMIT 1")
    suspend fun getScanRecordById(id: Long): ScanRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(record: ScanRecord): Long

    @Update
    suspend fun updateScanRecord(record: ScanRecord)

    @Delete
    suspend fun deleteScanRecord(record: ScanRecord)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAll()
}
