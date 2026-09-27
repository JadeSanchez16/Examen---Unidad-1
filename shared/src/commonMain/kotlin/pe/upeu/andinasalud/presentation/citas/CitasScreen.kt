package pe.upeu.andinasalud.presentation.citas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.usecase.FiltroEstado

@Composable
fun CitasScreen(viewModel: CitasViewModel, onDetalle: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val soloHoy by viewModel.soloHoy.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mis citas", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(busqueda, viewModel::buscar, Modifier.fillMaxWidth(), label = { Text("Buscar especialidad o médico") }, singleLine = true)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = soloHoy, onClick = { viewModel.seleccionarHoy(!soloHoy) }, label = { Text("Hoy") }) }
            items(FiltroEstado.entries) { item ->
                FilterChip(selected = filtro == item, onClick = { viewModel.filtrar(item) }, label = { Text(item.name) })
            }
        }
        when (val actual = state) {
            CitasUiState.Loading -> Text("Cargando citas…")
            CitasUiState.Empty -> Text(if (soloHoy) "No hay citas para hoy con estos filtros." else "No hay citas para este filtro.")
            is CitasUiState.Error -> {
                Text(actual.mensaje)
                TextButton(onClick = viewModel::cargar) { Text("Reintentar") }
            }
            is CitasUiState.Content -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(actual.citas, key = { it.id }) { cita -> CitaItem(cita, onClick = { onDetalle(cita.id) }) }
            }
        }
    }
}
