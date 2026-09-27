package pe.upeu.andinasalud.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Paciente

@Composable
fun PerfilScreen(paciente: Paciente, onAjustes: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        Text(paciente.nombre, style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()
        Text("Documento: ${paciente.documento}")
        Text("Correo: ${paciente.correo}")
        Text("Teléfono: ${paciente.telefono}")
        HorizontalDivider()
        OutlinedButton(onClick = onAjustes, modifier = Modifier.fillMaxWidth()) { Text("Ajustes") }
    }
}
