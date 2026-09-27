package pe.upeu.andinasalud.presentation.citas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita

fun Cita.fechaTexto(): String = fechaHora.date.toString()
fun Cita.horaTexto(): String = fechaHora.time.toString().take(5)
fun Cita.estadoTexto(): String = when (estado) {
    is EstadoCita.Programada -> "Programada"
    is EstadoCita.Atendida -> "Atendida"
    is EstadoCita.Cancelada -> "Cancelada"
}

@Composable
fun CitaItem(cita: Cita, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cita.especialidad, style = MaterialTheme.typography.titleMedium)
                Text(cita.estadoTexto(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(cita.medico, style = MaterialTheme.typography.bodyMedium)
            Text("${cita.sede}  ·  ${cita.fechaTexto()}  ·  ${cita.horaTexto()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
