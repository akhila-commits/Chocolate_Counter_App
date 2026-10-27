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

@Database(entities = [Product::class, ScanRecord::class], version = 3, exportSchema = false)
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
                    // --- Slabs (Includes items bearing the same name but different grammes) ---
                    Product(
                        name = "70% Dark Chocolate Slab",
                        category = "Slabs",
                        size = "100g",
                        price = 650.0,
                        description = "Rich 70% single-origin dark chocolate artisan slab",
                        shape = "Square",
                        colorHex = "#22110B",
                        isDefault = true
                    ),
                    Product(
                        name = "70% Dark Chocolate Slab",
                        category = "Slabs",
                        size = "250g",
                        price = 1450.0,
                        description = "Large sharing 70% dark chocolate artisan slab",
                        shape = "Square",
                        colorHex = "#22110B",
                        isDefault = true
                    ),
                    Product(
                        name = "Roasted Almond Milk Slab",
                        category = "Slabs",
                        size = "100g",
                        price = 700.0,
                        description = "Velvety milk chocolate loaded with whole roasted California almonds",
                        shape = "Square",
                        colorHex = "#5A3825",
                        isDefault = true
                    ),
                    Product(
                        name = "Roasted Almond Milk Slab",
                        category = "Slabs",
                        size = "200g",
                        price = 1300.0,
                        description = "Double-size milk chocolate slab with roasted caramelized almonds",
                        shape = "Square",
                        colorHex = "#5A3825",
                        isDefault = true
                    ),
                    Product(
                        name = "Caramelized White Slab",
                        category = "Slabs",
                        size = "100g",
                        price = 680.0,
                        description = "Golden caramelized white chocolate slab with sea salt flakes",
                        shape = "Square",
                        colorHex = "#C87D32",
                        isDefault = true
                    ),

                    // --- Chocolate pieces ---
                    Product(
                        name = "Hazelnut Truffle",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 250.0,
                        description = "Round milk chocolate ball rolled in crunchy toasted hazelnut flakes",
                        shape = "Truffle",
                        colorHex = "#4E2E1E",
                        isDefault = true
                    ),
                    Product(
                        name = "Dark Cacao Ganache",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 220.0,
                        description = "Glossy 70% dark chocolate dome with gold leaf flake",
                        shape = "Dome",
                        colorHex = "#22110B",
                        isDefault = true
                    ),
                    Product(
                        name = "Salted Caramel Dome",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 280.0,
                        description = "Smooth milk chocolate dome with amber salted caramel drizzle",
                        shape = "Dome",
                        colorHex = "#C87D32",
                        isDefault = true
                    ),
                    Product(
                        name = "Ruby Raspberry Heart",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 290.0,
                        description = "Velvety ruby pink chocolate heart dusted with freeze-dried raspberry",
                        shape = "Heart",
                        colorHex = "#B83A4B",
                        isDefault = true
                    ),
                    Product(
                        name = "Pistachio Praline",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 300.0,
                        description = "Dark chocolate cube layered with roasted Sicilian pistachio cream & green nibs",
                        shape = "Cube",
                        colorHex = "#396B47",
                        isDefault = true
                    ),
                    Product(
                        name = "White Passion Fruit",
                        category = "Chocolate pieces",
                        size = "1 Pc",
                        price = 260.0,
                        description = "Ivory white chocolate square with tangy yellow passion fruit swirl",
                        shape = "Square",
                        colorHex = "#F5ECD7",
                        isDefault = true
                    ),

                    // --- Beverages ---
                    Product(
                        name = "Signature Hot Chocolate",
                        category = "Beverages",
                        size = "Regular",
                        price = 550.0,
                        description = "Warm decadent molten chocolate drink with steamed fresh milk",
                        shape = "Dome",
                        colorHex = "#3B1E15",
                        isDefault = true
                    ),
                    Product(
                        name = "Signature Hot Chocolate",
                        category = "Beverages",
                        size = "Large",
                        price = 750.0,
                        description = "Extra large Belgian molten hot chocolate topped with toasted marshmallow",
                        shape = "Dome",
                        colorHex = "#3B1E15",
                        isDefault = true
                    ),
                    Product(
                        name = "Iced Dark Chocolate Mocha",
                        category = "Beverages",
                        size = "Regular",
                        price = 650.0,
                        description = "Espresso and melted dark chocolate over ice with chocolate drizzle",
                        shape = "Cube",
                        colorHex = "#2E1A11",
                        isDefault = true
                    ),
                    Product(
                        name = "Artisan Cold Brew & Cacao",
                        category = "Beverages",
                        size = "Regular",
                        price = 600.0,
                        description = "Steeped cold brew coffee infused with roasted cacao nibs",
                        shape = "Truffle",
                        colorHex = "#1F0E07",
                        isDefault = true
                    ),

                    // --- Gift boxes ---
                    Product(
                        name = "Luxury Truffle Box",
                        category = "Gift boxes",
                        size = "Box of 6",
                        price = 1600.0,
                        description = "Gold embossed gift box with 6 handcrafted assorted truffles",
                        shape = "Square",
                        colorHex = "#D4AF37",
                        isDefault = true
                    ),
                    Product(
                        name = "Luxury Truffle Box",
                        category = "Gift boxes",
                        size = "Box of 12",
                        price = 3100.0,
                        description = "Luxury keepsake gift box with 12 handcrafted assorted truffles",
                        shape = "Square",
                        colorHex = "#D4AF37",
                        isDefault = true
                    ),
                    Product(
                        name = "Luxury Truffle Box",
                        category = "Gift boxes",
                        size = "Box of 24",
                        price = 5800.0,
                        description = "Grand presenter gift box with 24 signature chocolates & truffles",
                        shape = "Square",
                        colorHex = "#D4AF37",
                        isDefault = true
                    ),

                    // --- Assortment packs ---
                    Product(
                        name = "Choco Bliss Assortment Pack",
                        category = "Assortment packs",
                        size = "200g",
                        price = 1200.0,
                        description = "Assortment pouch of mixed milk, dark and white chocolate bites",
                        shape = "Rocher",
                        colorHex = "#6B412B",
                        isDefault = true
                    ),
                    Product(
                        name = "Choco Bliss Assortment Pack",
                        category = "Assortment packs",
                        size = "400g",
                        price = 2200.0,
                        description = "Family pack of assorted handcrafted chocolate bites & clusters",
                        shape = "Rocher",
                        colorHex = "#6B412B",
                        isDefault = true
                    ),
                    Product(
                        name = "Artisan Selection Pack",
                        category = "Assortment packs",
                        size = "300g",
                        price = 1950.0,
                        description = "Master chocolatier's choice selection of seasonal pralines",
                        shape = "Square",
                        colorHex = "#4A2518",
                        isDefault = true
                    ),

                    // --- Cakes ---
                    Product(
                        name = "Belgian Chocolate Fudge Cake",
                        category = "Cakes",
                        size = "500g",
                        price = 2200.0,
                        description = "Moist layered Belgian chocolate fudge cake with dark chocolate ganache",
                        shape = "Square",
                        colorHex = "#1D0C07",
                        isDefault = true
                    ),
                    Product(
                        name = "Belgian Chocolate Fudge Cake",
                        category = "Cakes",
                        size = "1kg",
                        price = 4000.0,
                        description = "Celebration 1kg rich Belgian chocolate cake with chocolate curls",
                        shape = "Square",
                        colorHex = "#1D0C07",
                        isDefault = true
                    ),
                    Product(
                        name = "Dark Chocolate Mousse Slice",
                        category = "Cakes",
                        size = "1 Slice",
                        price = 750.0,
                        description = "Silky French chocolate mousse on almond sponge with cocoa glaze",
                        shape = "Square",
                        colorHex = "#2E1710",
                        isDefault = true
                    ),

                    // --- Savory Items ---
                    Product(
                        name = "Chocolate Chili Savory Tart",
                        category = "Savory Items",
                        size = "1 Pc",
                        price = 450.0,
                        description = "Flaky shortcrust tart with dark cacao, spiced beef & chili infusion",
                        shape = "Dome",
                        colorHex = "#5E1914",
                        isDefault = true
                    ),
                    Product(
                        name = "Cacao Spiced Croissant",
                        category = "Savory Items",
                        size = "1 Pc",
                        price = 380.0,
                        description = "Buttery croissant dusted with savory cacao sea salt & herbs",
                        shape = "Rocher",
                        colorHex = "#7A4E2D",
                        isDefault = true
                    ),
                    Product(
                        name = "Smoked Salmon & Cacao Brioche",
                        category = "Savory Items",
                        size = "1 Pc",
                        price = 650.0,
                        description = "Toasted brioche with smoked salmon, cream cheese and cocoa nib garnish",
                        shape = "Square",
                        colorHex = "#B85D36",
                        isDefault = true
                    ),

                    // --- Ice cream ---
                    Product(
                        name = "Dark Chocolate Gelato",
                        category = "Ice cream",
                        size = "1 Scoop",
                        price = 350.0,
                        description = "Single scoop of authentic Italian 72% dark chocolate gelato",
                        shape = "Dome",
                        colorHex = "#25120B",
                        isDefault = true
                    ),
                    Product(
                        name = "Dark Chocolate Gelato",
                        category = "Ice cream",
                        size = "2 Scoops",
                        price = 600.0,
                        description = "Double scoop of authentic Italian 72% dark chocolate gelato",
                        shape = "Dome",
                        colorHex = "#25120B",
                        isDefault = true
                    ),
                    Product(
                        name = "Dark Chocolate Gelato Tub",
                        category = "Ice cream",
                        size = "500ml",
                        price = 1400.0,
                        description = "Take-home 500ml tub of churned artisan dark chocolate gelato",
                        shape = "Cube",
                        colorHex = "#25120B",
                        isDefault = true
                    ),
                    Product(
                        name = "Vanilla Bean Choc Flake Gelato",
                        category = "Ice cream",
                        size = "1 Scoop",
                        price = 350.0,
                        description = "Bourbon vanilla bean gelato loaded with dark chocolate shavings",
                        shape = "Dome",
                        colorHex = "#F3E9DA",
                        isDefault = true
                    )
                )
                dao.insertAll(defaultProducts)
            }
        }
    }
}
