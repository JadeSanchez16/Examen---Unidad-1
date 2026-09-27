package pe.upeu.andinasalud.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.presentation.citas.CitaItem

@Composable
fun InicioScreen(paciente: Paciente, proxima: Cita?, onCitas: () -> Unit, onSolicitud: () -> Unit, onDetalle: (String) -> Unit, onReintentar: () -> Unit, error: String?) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Hola, ${paciente.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineMedium)
        Text("Tu próxima cita", style = MaterialTheme.typography.titleLarge)
        if (error != null) {
            Text(error)
            TextButton(onClick = onReintentar) { Text("Reintentar") }
        } else if (proxima != null) {
            CitaItem(proxima, onClick = { onDetalle(proxima.id) })
        } else {
            Text("No tienes citas programadas.")
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCitas, modifier = Modifier.fillMaxWidth()) { Text("Mis citas") }
            OutlinedButton(onClick = onSolicitud, modifier = Modifier.fillMaxWidth()) { Text("Solicitar cita") }
        }
    }
}
