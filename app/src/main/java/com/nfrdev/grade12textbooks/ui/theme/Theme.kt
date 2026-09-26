package com.nfrdev.grade12textbooks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF006874),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFB2EBF2),
        onPrimaryContainer = Color(0xFF001F24),
        secondary = Color(0xFF4A6267),
        secondaryContainer = Color(0xFFCCE8EC),
        tertiary = Color(0xFF52618A),
        tertiaryContainer = Color(0xFFDCE1FF),
        background = Color(0xFFF7FAFA),
        surface = Color(0xFFF7FAFA),
        surfaceVariant = Color(0xFFDEE8E9),
        onSurface = Color(0xFF171D1E),
        onSurfaceVariant = Color(0xFF3F494A),
        outline = Color(0xFF6F797A)
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFF75D1DE),
        onPrimary = Color(0xFF00363D),
        primaryContainer = Color(0xFF004F58),
        onPrimaryContainer = Color(0xFFB2EBF2),
        secondary = Color(0xFFB1CBD0),
        secondaryContainer = Color(0xFF334B50),
        tertiary = Color(0xFFBAC5F2),
        tertiaryContainer = Color(0xFF3B476E),
        background = Color(0xFF101415),
        surface = Color(0xFF101415),
        surfaceVariant = Color(0xFF3F494A),
        onSurface = Color(0xFFE0E3E3),
        onSurfaceVariant = Color(0xFFC0C9CA),
        outline = Color(0xFF899394)
    )
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = Typography(),
        content = content
    )
}
