package pe.upeu.andinasalud.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Paciente

@Composable
fun PerfilScreen(paciente: Paciente, oscuro: Boolean, onTema: (Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        Text(paciente.nombre, style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Documento: ${paciente.documento}")
            Text("Correo: ${paciente.correo}")
            Text("Teléfono: ${paciente.telefono}")
        }
        HorizontalDivider()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Modo claro", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = oscuro,
                onCheckedChange = onTema,
                modifier = Modifier.semantics { contentDescription = "Modo oscuro" },
            )
            Text("Modo oscuro", modifier = Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
