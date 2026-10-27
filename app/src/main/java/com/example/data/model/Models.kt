package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double, // Price per piece in LKR
    val description: String, // Visual cues for staff and AI recognition
    val shape: String = "Truffle", // Truffle, Dome, Square, Heart, Rocher, Cube
    val colorHex: String = "#3B1E15",
    val referenceImageUri: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ScanItem(
    val productName: String,
    val unitPrice: Double, // Snapshot of unit price at scan time
    val count: Int,
    val confidence: Double = 0.9,
    val shape: String = "Truffle",
    val colorHex: String = "#3B1E15"
) {
    val lineTotal: Double
        get() = count * unitPrice
}

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val assortmentName: String = "Assorted Tray",
    val photoUri: String? = null,
    val sampleTrayTitle: String? = null,
    val totalAmount: Double, // Total price in LKR calculated in app code
    val totalPieces: Int,
    val typesCount: Int,
    val summaryText: String,
    val itemsJson: String, // Serialized List<ScanItem>
    val notes: String = ""
)

data class DetectedItem(
    val productName: String,
    val count: Int,
    val confidence: Double = 0.85
)

data class RecognitionResult(
    val items: List<DetectedItem> = emptyList(),
    val unrecognizedCount: Int = 0,
    val notes: String = "",
    val rawJson: String = ""
)
