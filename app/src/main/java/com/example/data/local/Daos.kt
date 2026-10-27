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
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getProductList(): List<Product>

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

    @Query("SELECT * FROM scan_records WHERE LOWER(assortmentName) = LOWER(:assortment) ORDER BY timestamp DESC")
    fun getScanRecordsByAssortment(assortment: String): Flow<List<ScanRecord>>

    @Query("SELECT DISTINCT assortmentName FROM scan_records WHERE assortmentName IS NOT NULL AND assortmentName != '' ORDER BY assortmentName ASC")
    fun getDistinctAssortments(): Flow<List<String>>

    @Query("SELECT * FROM scan_records WHERE id = :id LIMIT 1")
    suspend fun getScanRecordById(id: Long): ScanRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(record: ScanRecord): Long

    @Delete
    suspend fun deleteScanRecord(record: ScanRecord)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAll()
}
