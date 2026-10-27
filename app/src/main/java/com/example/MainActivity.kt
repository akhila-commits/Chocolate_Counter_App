package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.scan.ScanScreen
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.ScanViewModel

enum class AppDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SCAN("Counter Scan", Icons.Default.CameraAlt, "nav_scan"),
    CATALOG("Catalog", Icons.Default.Category, "nav_catalog"),
    HISTORY("History", Icons.Default.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    private val scanViewModel: ScanViewModel by viewModels()
    private val catalogViewModel: CatalogViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    scanViewModel = scanViewModel,
                    catalogViewModel = catalogViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    scanViewModel: ScanViewModel,
    catalogViewModel: CatalogViewModel,
    historyViewModel: HistoryViewModel
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.SCAN) }
    val catalogProducts by catalogViewModel.allProducts.collectAsStateWithLifecycle()

    // Handle back button on secondary screens
    if (currentDestination != AppDestination.SCAN) {
        BackHandler {
            currentDestination = AppDestination.SCAN
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = CacaoBlack,
        bottomBar = {
            NavigationBar(
                containerColor = CacaoDark,
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 6.dp
            ) {
                AppDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CacaoBlack,
                            selectedTextColor = GoldLight,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = GoldDark,
                            unselectedTextColor = Color(0xFFBCAAA4)
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { destination ->
            when (destination) {
                AppDestination.SCAN -> ScanScreen(
                    viewModel = scanViewModel,
                    catalogProducts = catalogProducts
                )
                AppDestination.CATALOG -> CatalogScreen(
                    viewModel = catalogViewModel
                )
                AppDestination.HISTORY -> HistoryScreen(
                    viewModel = historyViewModel
                )
            }
        }
    }
}
