package pe.upeu.andinasalud.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun AjustesScreen(oscuro: Boolean, onTema: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        TemaSelector(oscuro, onTema)
    }
}

@Composable
fun TemaSelector(oscuro: Boolean, onTema: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Modo claro")
        Switch(checked = oscuro, onCheckedChange = onTema, modifier = Modifier.semantics { contentDescription = "Modo oscuro" })
        Text("Modo oscuro")
    }
}
