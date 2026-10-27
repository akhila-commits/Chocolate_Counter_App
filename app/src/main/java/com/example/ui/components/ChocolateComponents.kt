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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.CacaoBlack
import com.example.ui.theme.CacaoBorder
import com.example.ui.theme.CacaoCard
import com.example.ui.theme.CacaoDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PlayfairFontFamily
import java.io.File

@Composable
fun ChocolateThumbnail(
    shape: String,
    colorHex: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
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
                contentDescription = "Chocolate Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        ChocolateShapeBadge(
            shape = shape,
            colorHex = colorHex,
            modifier = modifier,
            size = size
        )
    }
}

@Composable
fun ChocolateShapeBadge(
    shape: String,
    colorHex: String,
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

private fun DrawScope.drawChocolateDome(baseColor: Color, w: Float, h: Float) {
    // Drop shadow
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(w * 0.1f, h * 0.65f),
        size = Size(w * 0.8f, h * 0.3f)
    )
    // Main dome gradient
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(baseColor.copy(alpha = 1f), Color(0xFF140804)),
            center = Offset(w * 0.4f, h * 0.35f),
            radius = w * 0.5f
        ),
        topLeft = Offset(w * 0.1f, h * 0.15f),
        size = Size(w * 0.8f, h * 0.7f)
    )
    // Gold caramel swirl on dome
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
    // Textured specks (cacao dust / hazelnut crumbs)
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
    // Passion fruit yellow swirl
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
    // Irregular jagged rocher cluster
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
    // Darker bottom facet
    drawRect(
        color = Color(0x44000000),
        topLeft = Offset(w * 0.15f, h * 0.55f),
        size = Size(w * 0.7f, h * 0.3f)
    )
    // Top highlight line
    drawLine(
        color = GoldPrimary.copy(alpha = 0.8f),
        start = Offset(w * 0.2f, h * 0.2f),
        end = Offset(w * 0.8f, h * 0.2f),
        strokeWidth = w * 0.06f
    )
}

@Composable
fun RevelloBrandBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CacaoBlack,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REVELLO SPECIALITY OUTLET",
                    fontFamily = PlayfairFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.2.sp,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Handcrafted Chocolates • Kalutara Counter",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC7B299),
                fontSize = 11.sp,
                letterSpacing = 0.4.sp
            )
        }
    }
}

data class SampleTrayPreset(
    val id: String,
    val title: String,
    val description: String,
    val pieceCount: Int,
    val previewColor: Color
)

val SampleTrayPresets = listOf(
    SampleTrayPreset(
        id = "tray_deluxe_12",
        title = "Deluxe Assorted Box (12 pcs)",
        description = "4 Hazelnut Truffles, 3 Dark Ganache, 3 Salted Caramel, 2 Ruby Hearts",
        pieceCount = 12,
        previewColor = Color(0xFF6B412B)
    ),
    SampleTrayPreset(
        id = "tray_signature_8",
        title = "Signature Pralines (8 pcs)",
        description = "3 Pistachio Praline, 3 White Passion Fruit, 2 Ceylon Espresso",
        pieceCount = 8,
        previewColor = Color(0xFF381E15)
    ),
    SampleTrayPreset(
        id = "tray_boutique_16",
        title = "Grand Boutique Platter (16 pcs)",
        description = "Assortment of all 8 Revello varieties on silver tray",
        pieceCount = 16,
        previewColor = Color(0xFFB83A4B)
    )
)
