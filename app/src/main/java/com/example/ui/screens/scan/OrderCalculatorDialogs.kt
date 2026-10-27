package com.example.ui.screens.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ScanItem
import com.example.ui.components.QuantityStepper
import com.example.ui.components.SizeWeightBadge
import com.example.ui.components.formatLKR
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlayfairFontFamily

@Composable
fun OrderCalculatorDialog(
    orderNumber: String,
    scanItems: List<ScanItem>,
    grandTotal: Double,
    totalPieces: Int,
    typesCount: Int,
    tenderAmount: Double?,
    changeDue: Double,
    notes: String,
    onNotesChanged: (String) -> Unit,
    onTenderAmountChanged: (Double?) -> Unit,
    onIncrement: (ScanItem) -> Unit,
    onDecrement: (ScanItem) -> Unit,
    onRemove: (ScanItem) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onViewReceipt: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            color = CacaoBlack,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CacaoDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Order #$orderNumber",
                                fontFamily = PlayfairFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = GoldLight
                            )
                            Text(
                                text = "$totalPieces items • $typesCount chocolate types",
                                color = Color(0xFFC7B299),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.testTag("clear_order_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear all",
                                tint = Color(0xFFB83A4B)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_order_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                HorizontalDivider(color = CacaoBorder)

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "ORDER ITEMS",
                            color = Color(0xFFA1887F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (scanItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your order is empty. Add products from the list.",
                                    color = Color(0xFF9E8E81),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(scanItems, key = { it.itemKey }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CacaoCard),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CacaoBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (item.size.isNotBlank()) {
                                                SizeWeightBadge(size = item.size)
                                            }
                                            Text(
                                                text = "${formatLKR(item.unitPrice)} each",
                                                color = Color(0xFFD7CCC8),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Line: ${formatLKR(item.lineTotal)}",
                                            color = GoldLight,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    QuantityStepper(
                                        quantity = item.count,
                                        onIncrement = { onIncrement(item) },
                                        onDecrement = { onDecrement(item) },
                                        testTagPrefix = "dialog_${item.itemKey}"
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onRemove(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove item",
                                            tint = Color(0xFFE57373),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cash Calculator Section
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CacaoDark),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CASH TENDER & CHANGE CALCULATOR",
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val quickCashOptions = remember(grandTotal) {
                                    val roundedHundreds = (Math.ceil(grandTotal / 500.0) * 500.0)
                                    listOf(
                                        grandTotal,
                                        roundedHundreds,
                                        (roundedHundreds + 500.0),
                                        (roundedHundreds + 1000.0)
                                    ).distinct().filter { it > 0 }
                                }

                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickCashOptions.forEach { cash ->
                                        val isExact = cash == grandTotal
                                        OutlinedButton(
                                            onClick = { onTenderAmountChanged(cash) },
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (tenderAmount == cash) GoldPrimary else CacaoBorder
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (tenderAmount == cash) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                                                contentColor = GoldLight
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = if (isExact) "Exact (${formatLKR(cash)})" else formatLKR(cash),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = tenderAmount?.toString() ?: "",
                                    onValueChange = { str ->
                                        onTenderAmountChanged(str.toDoubleOrNull())
                                    },
                                    label = { Text("Tendered Cash (Rs.)", color = Color(0xFFD7CCC8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CacaoCard,
                                        unfocusedContainerColor = CacaoCard,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CacaoBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                if (tenderAmount != null && tenderAmount >= grandTotal) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1B3B22), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "CHANGE DUE TO CUSTOMER:",
                                            color = Color(0xFFA5D6A7),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = formatLKR(changeDue),
                                            color = Color(0xFF81C784),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Order Notes
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChanged,
                            label = { Text("Order Notes / Special Requests (Optional)", color = Color(0xFFD7CCC8)) },
                            placeholder = { Text("e.g. Gift wrapped, ribbon, customer phone...", color = Color(0xFF8D6E63)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CacaoDark,
                                unfocusedContainerColor = CacaoDark,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = CacaoBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                HorizontalDivider(color = CacaoBorder)

                // Bottom Action Drawer: Total & Save
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CacaoDark,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "GRAND TOTAL",
                                    color = Color(0xFFA1887F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = formatLKR(grandTotal),
                                    fontFamily = PlayfairFontFamily,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onViewReceipt,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GoldPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                modifier = Modifier.testTag("preview_receipt_button")
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Receipt")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onSave,
                            enabled = scanItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = CacaoBlack,
                                disabledContainerColor = Color(0xFF4A3B2C),
                                disabledContentColor = Color(0xFF8D6E63)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_order_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save & Complete Order (#$orderNumber)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditOrderNumberDialog(
    currentOrderNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentOrderNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CacaoCard,
        title = {
            Text("Edit Order Number", color = GoldLight, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Enter custom order or invoice reference number:",
                    color = Color(0xFFD7CCC8),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CacaoDark,
                        unfocusedContainerColor = CacaoDark,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CacaoBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoldLight)
            }
        }
    )
}

@Composable
fun OrderReceiptDialog(
    orderNumber: String,
    scanItems: List<ScanItem>,
    grandTotal: Double,
    totalPieces: Int,
    tenderAmount: Double?,
    changeDue: Double,
    notes: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CacaoCard,
        title = {
            Text(
                text = "Order Receipt (#$orderNumber)",
                fontFamily = PlayfairFontFamily,
                color = GoldLight,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Revello Speciality Outlet - Kalutara", color = Color(0xFFA1887F), fontSize = 11.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CacaoBorder)

                scanItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.count}x ${item.displayName}",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatLKR(item.lineTotal),
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CacaoBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL PIECES:", color = Color(0xFFD7CCC8), fontSize = 12.sp)
                    Text("$totalPieces", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GRAND TOTAL:", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(formatLKR(grandTotal), color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (tenderAmount != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CASH TENDERED:", color = Color(0xFFD7CCC8), fontSize = 12.sp)
                        Text(formatLKR(tenderAmount), color = Color.White, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CHANGE DUE:", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(formatLKR(changeDue), color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Note: $notes", color = Color(0xFFBCAAA4), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CacaoBlack)
            ) {
                Text("Close")
            }
        }
    )
}
