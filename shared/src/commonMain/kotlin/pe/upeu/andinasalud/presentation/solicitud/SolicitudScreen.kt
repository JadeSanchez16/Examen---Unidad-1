package pe.upeu.andinasalud.presentation.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.ModalidadAtencion

@Composable
private fun Selector(etiqueta: String, valor: String, opciones: List<String>, error: String?, onSeleccion: (String) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expandido = true }, modifier = Modifier.fillMaxWidth()) { Text("$etiqueta: ${valor.ifBlank { "Seleccionar" }}") }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { onSeleccion(opcion); expandido = false }) }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SolicitudScreen(viewModel: SolicitudViewModel, onCompletada: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Solicitar cita", style = MaterialTheme.typography.headlineMedium)
        if (state.completada) {
            Text("Tu cita fue programada.")
            Button(onClick = { viewModel.reiniciar(); onCompletada() }, modifier = Modifier.fillMaxWidth()) { Text("Ver mis citas") }
        } else {
            if (state.especialidades.isEmpty() && state.errorGeneral == null) Text("Cargando opciones…")
            Selector("Especialidad", state.especialidad, state.especialidades, state.errores.especialidad, viewModel::especialidad)
            Selector("Sede", state.sede, state.sedes, state.errores.sede, viewModel::sede)
            Selector("Modalidad", state.modalidad.name, ModalidadAtencion.entries.map { it.name }, null) { viewModel.modalidad(ModalidadAtencion.valueOf(it)) }
            OutlinedTextField(state.fecha, viewModel::fecha, Modifier.fillMaxWidth(), label = { Text("Fecha (AAAA-MM-DD)") }, isError = state.errores.fecha != null, supportingText = { state.errores.fecha?.let { Text(it) } }, singleLine = true)
            OutlinedTextField(state.hora, viewModel::hora, Modifier.fillMaxWidth(), label = { Text("Hora (HH:MM)") }, isError = state.errores.hora != null, supportingText = { state.errores.hora?.let { Text(it) } }, singleLine = true)
            OutlinedTextField(state.motivo, viewModel::motivo, Modifier.fillMaxWidth(), label = { Text("Motivo de consulta") }, isError = state.errores.motivo != null, supportingText = { state.errores.motivo?.let { Text(it) } }, minLines = 3, maxLines = 6)
            if (state.errorGeneral != null) {
                Text(state.errorGeneral ?: "", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::cargarOpciones) { Text("Reintentar") }
            }
            Button(onClick = viewModel::enviar, enabled = !state.enviando && state.especialidades.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text(if (state.enviando) "Guardando…" else "Solicitar cita")
            }
        }
    }
}
