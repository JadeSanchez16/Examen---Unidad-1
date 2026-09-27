package pe.upeu.andinasalud.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.presentation.citas.estadoTexto
import pe.upeu.andinasalud.presentation.citas.fechaTexto
import pe.upeu.andinasalud.presentation.citas.horaTexto

@Composable
fun DetalleCitaScreen(id: String, viewModel: DetalleCitaViewModel) {
    val state by viewModel.uiState.collectAsState()
    var confirmar by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Detalle de cita", style = MaterialTheme.typography.headlineMedium)
        when (val actual = state) {
            DetalleUiState.Loading -> Text("Cargando detalle…")
            DetalleUiState.Empty -> Text("Cita no encontrada.")
            is DetalleUiState.Error -> {
                Text(actual.mensaje)
                TextButton(onClick = { viewModel.cargar(id) }) { Text("Reintentar") }
            }
            is DetalleUiState.Content -> {
                val cita = actual.cita
                Text(cita.especialidad, style = MaterialTheme.typography.titleLarge)
                Text("Médico: ${cita.medico}")
                Text("Sede: ${cita.sede}")
                Text("Fecha: ${cita.fechaTexto()}")
                Text("Hora: ${cita.horaTexto()}")
                Text("Estado: ${cita.estadoTexto()}")
                Text("Motivo: ${cita.motivo}")
                Text("Indicaciones: ${(cita.estado as? EstadoCita.Atendida)?.indicaciones ?: "Sin indicaciones"}")
                if (actual.mensaje != null) Text(actual.mensaje, color = MaterialTheme.colorScheme.primary)
                if (cita.estado is EstadoCita.Programada) {
                    Button(onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar cita") }
                }
            }
        }
    }
    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false },
        title = { Text("Cancelar cita") },
        text = { Text("¿Confirmas que deseas cancelar esta cita?") },
        confirmButton = { TextButton(onClick = { confirmar = false; viewModel.cancelar(id) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Volver") } },
    )
}
