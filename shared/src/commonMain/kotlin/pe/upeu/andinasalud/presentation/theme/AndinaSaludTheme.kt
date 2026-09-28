package pe.upeu.andinasalud.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EADD),
    onPrimaryContainer = Color(0xFF0D392F),
    secondary = Coral,
    secondaryContainer = Color(0xFFF7DDD7),
    onSecondaryContainer = Color(0xFF4F241B),
    background = FondoClaro,
    onBackground = Color(0xFF1B2421),
    surface = Color.White,
    onSurface = Color(0xFF1B2421),
    surfaceVariant = Color(0xFFE7EFEA),
    onSurfaceVariant = Color(0xFF3D5148),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEF4F1),
    surfaceContainer = Color(0xFFE7EFEA),
    surfaceContainerHigh = Color(0xFFDDE8E2),
    outline = Color(0xFF71837A),
    outlineVariant = Color(0xFFB7C9BF),
)
private val Oscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = FondoOscuro,
    primaryContainer = Color(0xFF225446),
    onPrimaryContainer = Color(0xFFD4EADD),
    secondary = Color(0xFFFFB4A5),
    secondaryContainer = Color(0xFF6C3D34),
    onSecondaryContainer = Color(0xFFFFDAD2),
    background = FondoOscuro,
    onBackground = Color(0xFFE6EEE9),
    surface = Color(0xFF1D2622),
    onSurface = Color(0xFFE6EEE9),
    surfaceVariant = Color(0xFF2E3C35),
    onSurfaceVariant = Color(0xFFC0D1C7),
    surfaceContainerLowest = FondoOscuro,
    surfaceContainerLow = Color(0xFF1D2923),
    surfaceContainer = Color(0xFF23332B),
    surfaceContainerHigh = Color(0xFF2B4035),
    outline = Color(0xFF8AA89A),
    outlineVariant = Color(0xFF485C51),
)

@Composable
fun AndinaSaludTheme(oscuro: Boolean, contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (oscuro) Oscuro else Claro, typography = AndinaTipografia, content = contenido)
}
