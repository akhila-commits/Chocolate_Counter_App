package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.AmberCaramel
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlayfairFontFamily
import java.io.File
import java.text.NumberFormat
import java.util.Locale

fun formatLKR(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "Rs. " + formatter.format(amount)
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "chocolate pieces", "chocolate piece" -> Icons.Default.Cookie
        "beverages", "beverage" -> Icons.Default.LocalCafe
        "slabs", "slab" -> Icons.Default.ViewAgenda
        "gift boxes", "gift box" -> Icons.Default.CardGiftcard
        "assortment packs", "assortment pack" -> Icons.Default.AllInbox
        "cakes", "cake" -> Icons.Default.Cake
        "savory items", "savory item" -> Icons.Default.BakeryDining
        "ice cream", "gelato" -> Icons.Default.Icecream
        else -> Icons.Default.Cookie
    }
}

@Composable
fun SizeWeightBadge(
    size: String,
    modifier: Modifier = Modifier
) {
    if (size.isBlank()) return
    Surface(
        modifier = modifier,
        color = AmberCaramel.copy(alpha = 0.18f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberCaramel.copy(alpha = 0.6f))
    ) {
        Text(
            text = size,
            color = AmberCaramel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    if (category.isBlank()) return
    Surface(
        modifier = modifier,
        color = CacaoBorder.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category,
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "stepper"
) {
    Row(
        modifier = modifier
            .background(CacaoDark, RoundedCornerShape(8.dp))
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(36.dp)
                .testTag("${testTagPrefix}_decrement")
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease count",
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = "$quantity",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(36.dp)
                .testTag("${testTagPrefix}_increment")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase count",
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ChocolateThumbnail(
    shape: String,
    colorHex: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    category: String = ""
) {
    if (!imageUri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(CacaoDark)
                .border(1.5.dp, GoldPrimary, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (imageUri.startsWith("/")) File(imageUri) else imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        ChocolateShapeBadge(
            shape = shape,
            colorHex = colorHex,
            category = category,
            modifier = modifier,
            size = size
        )
    }
}

@Composable
fun ChocolateShapeBadge(
    shape: String,
    colorHex: String,
    category: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val baseColor = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(Color(0xFF3B1E15))

    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(CacaoDark, RoundedCornerShape(10.dp))
            .border(1.dp, CacaoBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (category.isNotBlank() && category != "Chocolate pieces") {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(size * 0.55f)
            )
        } else {
            Canvas(modifier = Modifier.size(size * 0.75f)) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height

                when (shape.lowercase()) {
                    "dome" -> drawChocolateDome(baseColor, canvasWidth, canvasHeight)
                    "truffle" -> drawChocolateTruffle(baseColor, canvasWidth, canvasHeight)
                    "square" -> drawChocolateSquare(baseColor, canvasWidth, canvasHeight)
                    "heart" -> drawChocolateHeart(baseColor, canvasWidth, canvasHeight)
                    "rocher" -> drawChocolateRocher(baseColor, canvasWidth, canvasHeight)
                    "cube" -> drawChocolateCube(baseColor, canvasWidth, canvasHeight)
                    else -> drawChocolateTruffle(baseColor, canvasWidth, canvasHeight)
                }
            }
        }
    }
}

private fun DrawScope.drawChocolateDome(baseColor: Color, w: Float, h: Float) {
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(w * 0.1f, h * 0.65f),
        size = Size(w * 0.8f, h * 0.3f)
    )
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(baseColor, Color(0xFF140804)),
            center = Offset(w * 0.4f, h * 0.35f),
            radius = w * 0.5f
        ),
        topLeft = Offset(w * 0.1f, h * 0.15f),
        size = Size(w * 0.8f, h * 0.7f)
    )
    val path = Path().apply {
        moveTo(w * 0.25f, h * 0.45f)
        quadraticTo(w * 0.5f, h * 0.3f, w * 0.75f, h * 0.5f)
    }
    drawPath(path, color = GoldLight, style = Stroke(width = w * 0.08f))
}

private fun DrawScope.drawChocolateTruffle(baseColor: Color, w: Float, h: Float) {
    drawCircle(
        color = Color(0x66000000),
        center = Offset(w * 0.5f, h * 0.55f),
        radius = w * 0.42f
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(baseColor, Color(0xFF140804)),
            center = Offset(w * 0.38f, h * 0.38f),
            radius = w * 0.45f
        ),
        center = Offset(w * 0.5f, h * 0.5f),
        radius = w * 0.4f
    )
    drawCircle(color = GoldLight.copy(alpha = 0.8f), center = Offset(w * 0.4f, h * 0.35f), radius = w * 0.05f)
    drawCircle(color = Color(0x66FFFFFF), center = Offset(w * 0.62f, h * 0.42f), radius = w * 0.04f)
    drawCircle(color = GoldPrimary.copy(alpha = 0.7f), center = Offset(w * 0.48f, h * 0.6f), radius = w * 0.04f)
}

private fun DrawScope.drawChocolateSquare(baseColor: Color, w: Float, h: Float) {
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(w * 0.12f, h * 0.16f),
        size = Size(w * 0.76f, h * 0.76f),
        cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, Color(0xFF1A0A05)),
            start = Offset(w * 0.1f, h * 0.1f),
            end = Offset(w * 0.9f, h * 0.9f)
        ),
        topLeft = Offset(w * 0.12f, h * 0.12f),
        size = Size(w * 0.76f, h * 0.76f),
        cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
    )
    val swirl = Path().apply {
        moveTo(w * 0.25f, h * 0.35f)
        quadraticTo(w * 0.5f, h * 0.2f, w * 0.75f, h * 0.35f)
        quadraticTo(w * 0.5f, h * 0.65f, w * 0.35f, h * 0.75f)
    }
    drawPath(swirl, color = GoldLight, style = Stroke(width = w * 0.08f))
}

private fun DrawScope.drawChocolateHeart(baseColor: Color, w: Float, h: Float) {
    val heart = Path().apply {
        moveTo(w * 0.5f, h * 0.8f)
        cubicTo(w * 0.15f, h * 0.55f, w * 0.1f, h * 0.25f, w * 0.32f, h * 0.2f)
        cubicTo(w * 0.45f, h * 0.18f, w * 0.5f, h * 0.32f, w * 0.5f, h * 0.32f)
        cubicTo(w * 0.5f, h * 0.32f, w * 0.55f, h * 0.18f, w * 0.68f, h * 0.2f)
        cubicTo(w * 0.9f, h * 0.25f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.8f)
        close()
    }
    drawPath(
        path = heart,
        brush = Brush.radialGradient(
            colors = listOf(baseColor, Color(0xFF350B12)),
            center = Offset(w * 0.4f, h * 0.35f),
            radius = w * 0.5f
        )
    )
    drawCircle(color = Color(0x77FFFFFF), center = Offset(w * 0.36f, h * 0.32f), radius = w * 0.05f)
}

private fun DrawScope.drawChocolateRocher(baseColor: Color, w: Float, h: Float) {
    drawCircle(color = baseColor, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.36f)
    drawCircle(color = Color(0xFFD49A50), center = Offset(w * 0.35f, h * 0.38f), radius = w * 0.12f)
    drawCircle(color = Color(0xFF8A5328), center = Offset(w * 0.62f, h * 0.42f), radius = w * 0.14f)
    drawCircle(color = Color(0xFFB87838), center = Offset(w * 0.45f, h * 0.65f), radius = w * 0.13f)
    drawCircle(color = GoldLight, center = Offset(w * 0.55f, h * 0.35f), radius = w * 0.06f)
}

private fun DrawScope.drawChocolateCube(baseColor: Color, w: Float, h: Float) {
    drawRoundRect(
        color = baseColor,
        topLeft = Offset(w * 0.15f, h * 0.15f),
        size = Size(w * 0.7f, h * 0.7f),
        cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
    )
    drawRect(
        color = Color(0x44000000),
        topLeft = Offset(w * 0.15f, h * 0.55f),
        size = Size(w * 0.7f, h * 0.3f)
    )
    drawLine(
        color = GoldPrimary.copy(alpha = 0.8f),
        start = Offset(w * 0.2f, h * 0.2f),
        end = Offset(w * 0.8f, h * 0.2f),
        strokeWidth = w * 0.06f
    )
}

@Composable
fun RevelloBrandBanner(
    title: String = "REVELLO ORDER CALCULATOR",
    subtitle: String = "Speciality Outlet • Counter POS & Order Tally",
    showPoweredBy: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CacaoBlack,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontFamily = PlayfairFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.1.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC7B299),
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
            if (showPoweredBy) {
                Spacer(modifier = Modifier.height(6.dp))
                PoweredByMarkTechBadge(compact = true)
            }
        }
    }
}
