package pe.upeu.andinasalud.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(primary = Verde, onPrimary = Color.White, secondary = Coral, background = FondoClaro, surface = Color.White)
private val Oscuro = darkColorScheme(primary = VerdeClaro, onPrimary = FondoOscuro, secondary = Color(0xFFFFB4A5), background = FondoOscuro, surface = Color(0xFF202A27))

@Composable
fun AndinaSaludTheme(oscuro: Boolean, contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (oscuro) Oscuro else Claro, typography = AndinaTipografia, content = contenido)
}
