package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

val PRODUCT_CATEGORIES = listOf(
    "Chocolate pieces",
    "Beverages",
    "Slabs",
    "Gift boxes",
    "Assortment packs",
    "Cakes",
    "Savory Items",
    "Ice cream"
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Chocolate pieces",
    val size: String = "", // e.g. "100g", "250g", "500g", "1 Pc", "Regular", "Large"
    val price: Double, // Price in LKR
    val description: String = "",
    val shape: String = "Piece", // For visual icon/thumbnail
    val colorHex: String = "#3B1E15",
    val referenceImageUri: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (size.isNotBlank()) "$name ($size)" else name
}

@JsonClass(generateAdapter = true)
data class ScanItem(
    val productName: String,
    val unitPrice: Double, // Snapshot of unit price at order time
    val count: Int,
    val category: String = "Chocolate pieces",
    val size: String = "", // e.g. "100g", "250g"
    val productId: Long = 0,
    val confidence: Double = 1.0,
    val shape: String = "Piece",
    val colorHex: String = "#3B1E15"
) {
    val lineTotal: Double
        get() = count * unitPrice

    val itemKey: String
        get() = if (size.isNotBlank()) "$productName-$size" else productName

    val displayName: String
        get() = if (size.isNotBlank()) "$productName ($size)" else productName
}

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val orderNumber: String = "",
    val photoUri: String? = null,
    val sampleTrayTitle: String? = null,
    val totalAmount: Double, // Total price in LKR
    val totalPieces: Int,
    val typesCount: Int,
    val summaryText: String,
    val itemsJson: String, // Serialized List<ScanItem>
    val notes: String = ""
)

data class DetectedItem(
    val productName: String,
    val count: Int,
    val confidence: Double = 1.0
)

data class RecognitionResult(
    val items: List<DetectedItem> = emptyList(),
    val unrecognizedCount: Int = 0,
    val notes: String = "",
    val rawJson: String = ""
)
