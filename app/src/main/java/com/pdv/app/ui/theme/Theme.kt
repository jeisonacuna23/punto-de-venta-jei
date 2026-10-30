package com.pdv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MochaSilk = lightColorScheme(
    primary = Color(0xFF5D4037),           // Marrón chocolate
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFFFFFFF),
    
    secondary = Color(0xFF795548),         // Marrón medio
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF7F3ED), // Forzado a crema
    onSecondaryContainer = Color(0xFF30231F),
    
    tertiary = Color(0xFFB98250),          // Caramelo
    onTertiary = Color(0xFFFFFFFF),
    
    background = Color(0xFFF7F3ED),        // Crema suave
    onBackground = Color(0xFF30231F),      // Marrón oscuro
    
    surface = Color(0xFFFFFFFF),           // Blanco cálido
    onSurface = Color(0xFF30231F),
    
    // AQUÍ ESTABA EL MORADO: Forzamos todas las superficies a blanco
    surfaceVariant = Color(0xFFFFFFFF),    
    onSurfaceVariant = Color(0xFF30231F),
    surfaceTint = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    
    outline = Color(0xFFE5D8CA)            // Beige
)

@Composable
fun PdvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MochaSilk,
        content = content
    )
}
