package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Product
import com.example.data.model.ScanRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class, ScanRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun scanRecordDao(): ScanRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "revello_choc_counter.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialRevelloCatalog(database.productDao())
                    }
                }
            }
        }

        suspend fun populateInitialRevelloCatalog(dao: ProductDao) {
            if (dao.getCount() == 0) {
                val defaultProducts = listOf(
                    Product(
                        name = "Hazelnut Truffle",
                        price = 250.0,
                        description = "Round milk chocolate ball rolled in crunchy toasted hazelnut flakes",
                        shape = "Truffle",
                        colorHex = "#4E2E1E",
                        isDefault = true
                    ),
                    Product(
                        name = "Dark Cacao Ganache",
                        price = 220.0,
                        description = "Glossy 70% dark chocolate dome with gold leaf flake",
                        shape = "Dome",
                        colorHex = "#22110B",
                        isDefault = true
                    ),
                    Product(
                        name = "Salted Caramel Dome",
                        price = 280.0,
                        description = "Smooth milk chocolate dome with amber salted caramel drizzle",
                        shape = "Dome",
                        colorHex = "#C87D32",
                        isDefault = true
                    ),
                    Product(
                        name = "White Passion Fruit",
                        price = 260.0,
                        description = "Ivory white chocolate square with tangy yellow passion fruit swirl",
                        shape = "Square",
                        colorHex = "#F5ECD7",
                        isDefault = true
                    ),
                    Product(
                        name = "Pistachio Praline",
                        price = 300.0,
                        description = "Dark chocolate cube layered with roasted Sicilian pistachio cream & green nibs",
                        shape = "Cube",
                        colorHex = "#396B47",
                        isDefault = true
                    ),
                    Product(
                        name = "Almond Rocher",
                        price = 270.0,
                        description = "Irregular cluster of caramelized almonds drenched in dark chocolate",
                        shape = "Rocher",
                        colorHex = "#5A3825",
                        isDefault = true
                    ),
                    Product(
                        name = "Ruby Raspberry Heart",
                        price = 290.0,
                        description = "Velvety ruby pink chocolate heart dusted with freeze-dried raspberry",
                        shape = "Heart",
                        colorHex = "#B83A4B",
                        isDefault = true
                    ),
                    Product(
                        name = "Ceylon Espresso Cube",
                        price = 240.0,
                        description = "Dark chocolate cube filled with rich local Ceylon espresso ganache",
                        shape = "Cube",
                        colorHex = "#2E1A11",
                        isDefault = true
                    )
                )
                dao.insertAll(defaultProducts)
            }
        }
    }
}
