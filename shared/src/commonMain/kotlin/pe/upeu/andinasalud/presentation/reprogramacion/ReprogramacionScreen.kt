package pe.upeu.andinasalud.presentation.reprogramacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReprogramacionScreen(id: String, viewModel: ReprogramacionViewModel, onCompletada: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Reprogramar cita", style = MaterialTheme.typography.headlineMedium)
        when {
            state.cargando -> Text("Cargando cita…")
            state.completada -> {
                Text("Cita reprogramada correctamente.")
                Button(onClick = onCompletada, modifier = Modifier.fillMaxWidth()) { Text("Ver detalle") }
            }
            state.sinCita -> Text("Cita no encontrada.")
            state.errorCarga != null -> {
                Text(state.errorCarga ?: "", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { viewModel.cargar(id) }) { Text("Reintentar") }
            }
            else -> {
                OutlinedTextField(state.fecha, viewModel::fecha, Modifier.fillMaxWidth(), label = { Text("Nueva fecha (AAAA-MM-DD)") }, isError = state.errores.fecha != null, supportingText = { state.errores.fecha?.let { Text(it) } }, singleLine = true)
                OutlinedTextField(state.hora, viewModel::hora, Modifier.fillMaxWidth(), label = { Text("Nueva hora (HH:MM)") }, isError = state.errores.hora != null, supportingText = { state.errores.hora?.let { Text(it) } }, singleLine = true)
                if (state.errorGeneral != null) {
                    Text(state.errorGeneral ?: "", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = { viewModel.enviar(id) }, enabled = !state.enviando, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.enviando) "Guardando…" else "Confirmar nueva fecha")
                }
            }
        }
    }
}
