package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = CacaoBlack,
    primaryContainer = CacaoCard,
    onPrimaryContainer = GoldLight,
    secondary = AmberCaramel,
    onSecondary = Color.White,
    secondaryContainer = CacaoSurface,
    onSecondaryContainer = GoldLight,
    tertiary = GoldLight,
    onTertiary = CacaoBlack,
    background = CacaoBlack,
    onBackground = CreamBackground,
    surface = CacaoDark,
    onSurface = CreamBackground,
    surfaceVariant = CacaoSurface,
    onSurfaceVariant = Color(0xFFD7CCC8),
    outline = CacaoBorder,
    error = RubyBerryColor,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CacaoDark,
    onPrimary = GoldLight,
    primaryContainer = CreamCard,
    onPrimaryContainer = CacaoBlack,
    secondary = AmberCaramel,
    onSecondary = Color.White,
    secondaryContainer = CreamSurface,
    onSecondaryContainer = CacaoDark,
    tertiary = GoldPrimary,
    onTertiary = CacaoBlack,
    background = CreamBackground,
    onBackground = CreamText,
    surface = Color.White,
    onSurface = CreamText,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = CacaoMedium,
    outline = Color(0xFFD7CCC8),
    error = RubyBerryColor,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We default to the rich boutique chocolate palette (dark by default or system)
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Luxury dark chocolate provides optimal boutique branding

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
