package com.example.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ScanItem
import com.example.data.model.ScanRecord
import com.example.ui.components.ChocolateShapeBadge
import com.example.ui.components.PoweredByMarkTechFooter
import com.example.ui.components.RevelloBrandBanner
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.CacaoSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.RubyBerryColor
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.components.formatLKR
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.filteredScans.collectAsStateWithLifecycle()
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedScanForDetail by remember { mutableStateOf<ScanRecord?>(null) }
    var scanToDelete by remember { mutableStateOf<ScanRecord?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CacaoBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                RevelloBrandBanner()
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Order Tally History",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                    Text(
                        text = "Persisted counts, order numbers & price snapshots",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD7CCC8)
                    )
                }
            }

            // Search by Order Number
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_search_input"),
                        placeholder = { Text("Search by Order Number (e.g. ORD-1001)...", color = Color(0xFFBCAAA4), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GoldLight,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CacaoBorder,
                            focusedContainerColor = CacaoDark,
                            unfocusedContainerColor = CacaoDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Showing all ${allScans.size} order tallies" else "Showing ${scans.size} result${if (scans.size == 1) "" else "s"} for \"$searchQuery\"",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (scans.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CacaoCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = GoldLight, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No orders found matching '$searchQuery'" else "No saved order tallies yet",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 18.sp,
                            color = GoldLight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When shop counter staff tallies a tray of chocolates, save it with an order number to review it here.",
                            color = Color(0xFFBCAAA4),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(scans) { scan ->
                    ScanRecordCard(
                        scan = scan,
                        onClick = { selectedScanForDetail = scan },
                        onDelete = { scanToDelete = scan }
                    )
                }
            }

            item {
                PoweredByMarkTechFooter(
                    showTagline = true,
                    compact = true,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }

    // Detail Modal: Read-only breakdown of the scan with frozen price snapshots
    selectedScanForDetail?.let { scan ->
        val items = viewModel.parseScanItems(scan.itemsJson)
        val formattedDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))

        AlertDialog(
            onDismissRequest = { selectedScanForDetail = null },
            title = {
                Column {
                    Text(
                        text = "Order #${scan.orderNumber}",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 20.sp,
                        color = GoldLight
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD7CCC8)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Saved photo thumbnail if available
                    if (!scan.photoUri.isNullOrBlank()) {
                        val file = File(scan.photoUri)
                        if (file.exists()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, CacaoBorder, RoundedCornerShape(8.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(file)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Tray Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    LazyColumn(modifier = Modifier.height(220.dp)) {
                        items(items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChocolateShapeBadge(shape = item.shape, colorHex = item.colorHex, size = 32.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("${item.count} × ${formatLKR(item.unitPrice)}", color = Color(0xFFD7CCC8), fontSize = 12.sp)
                                }
                                Text(
                                    text = formatLKR(item.lineTotal),
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            HorizontalDivider(color = CacaoBorder.copy(alpha = 0.4f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary + Total
                    Surface(
                        color = CacaoBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Pieces:", color = Color(0xFFD7CCC8), fontSize = 13.sp)
                                Text("${scan.totalPieces} pieces (${scan.typesCount} types)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total:", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(formatLKR(scan.totalAmount), color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            PoweredByMarkTechFooter(
                                showTagline = false,
                                compact = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedScanForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CacaoDark
        )
    }

    // Delete Scan Confirmation Dialog
    scanToDelete?.let { scan ->
        AlertDialog(
            onDismissRequest = { scanToDelete = null },
            title = {
                Text("Delete this Scan Record?", fontFamily = PlayfairFontFamily, color = GoldLight)
            },
            text = {
                Text(
                    "This will remove the saved tally of ${formatLKR(scan.totalAmount)} for Order #${scan.orderNumber} from history.",
                    color = Color(0xFFD7CCC8)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScan(scan)
                        scanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyBerryColor, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { scanToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CacaoDark
        )
    }
}

@Composable
private fun ScanRecordCard(
    scan: ScanRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("scan_record_${scan.id}"),
        colors = CardDefaults.cardColors(containerColor = CacaoDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CacaoBorder, CacaoCard)))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail if exists, else luxury receipt badge
            if (!scan.photoUri.isNullOrBlank() && File(scan.photoUri).exists()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(scan.photoUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Tray Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CacaoSurface)
                        .border(1.dp, GoldDark.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order #${scan.orderNumber}",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    color = Color(0xFFD7CCC8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${scan.totalPieces} pcs across ${scan.typesCount} types",
                    color = Color(0xFFA1887F),
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatLKR(scan.totalAmount),
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = RubyBerryColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
