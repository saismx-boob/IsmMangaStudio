package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MangaCrimson,
    onPrimary = MangaOnCrimson,
    primaryContainer = MangaCrimsonContainer,
    onPrimaryContainer = MangaCrimson,
    secondary = ManhuaCyan,
    onSecondary = InkMidnight,
    secondaryContainer = ManhuaCyanContainer,
    onSecondaryContainer = ManhuaCyan,
    tertiary = QiGold,
    onTertiary = InkMidnight,
    tertiaryContainer = QiGoldContainer,
    onTertiaryContainer = QiGold,
    background = InkMidnight,
    onBackground = TextPrimary,
    surface = InkSurface,
    onSurface = TextPrimary,
    surfaceVariant = InkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = InkBorder
)

@Composable
fun MangaStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
