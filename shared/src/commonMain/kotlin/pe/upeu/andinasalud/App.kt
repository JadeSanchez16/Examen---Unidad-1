package pe.upeu.andinasalud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pe.upeu.andinasalud.presentation.navigation.AppNavHost
import pe.upeu.andinasalud.presentation.theme.AndinaSaludTheme

@Composable
fun App() {
    var oscuro by remember { mutableStateOf(false) }
    AndinaSaludTheme(oscuro) {
        AppNavHost(oscuro, { oscuro = it })
    }
}
