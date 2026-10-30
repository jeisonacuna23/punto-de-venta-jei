package com.pdv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MochaSilk = lightColorScheme(
    primary = Color(0xFF5D4037),      // Marrón chocolate
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF795548),    // Marrón medio
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB98250),     // Caramelo
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F3ED),   // Crema suave
    onBackground = Color(0xFF30231F), // Marrón oscuro
    surface = Color(0xFFFFFFFF),      // Blanco cálido
    onSurface = Color(0xFF30231F),
    surfaceVariant = Color(0xFFF7F3ED),
    onSurfaceVariant = Color(0xFF30231F),
    outline = Color(0xFFE5D8CA)       // Beige
)

@Composable
fun PdvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MochaSilk,
        content = content
    )
}
