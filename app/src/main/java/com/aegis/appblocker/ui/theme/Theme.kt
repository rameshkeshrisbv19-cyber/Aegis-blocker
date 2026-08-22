package com.aegis.appblocker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---- Aegis palette (unique deep-space violet / cyan) ----
val AegisBg = Color(0xFF0A0E1A)
val AegisSurface = Color(0xFF141A2E)
val AegisSurfaceHi = Color(0xFF1C2440)
val AegisPrimary = Color(0xFF6C63FF)
val AegisSecondary = Color(0xFF00E0C7)
val AegisAccent = Color(0xFF9D4EDD)
val AegisDanger = Color(0xFFFF5C7A)
val AegisTextHi = Color(0xFFF2F1FF)
val AegisTextLo = Color(0xFF8B88B0)

private val AegisColors = darkColorScheme(
    primary = AegisPrimary,
    secondary = AegisSecondary,
    tertiary = AegisAccent,
    background = AegisBg,
    surface = AegisSurface,
    surfaceVariant = AegisSurfaceHi,
    error = AegisDanger,
    onPrimary = Color.White,
    onBackground = AegisTextHi,
    onSurface = AegisTextHi
)

private val AegisType = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 15.sp),
    bodySmall = TextStyle(fontSize = 13.sp, color = AegisTextLo),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun AegisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AegisColors, typography = AegisType, content = content)
}
