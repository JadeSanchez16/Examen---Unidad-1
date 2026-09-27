package pe.upeu.andinasalud.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase

enum class FiltroEstado { Todas, Programada, Atendida, Cancelada }

class CitasViewModel(private val obtenerCitas: ObtenerCitasUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<CitasUiState>(CitasUiState.Loading)
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()
    private val _filtro = MutableStateFlow(FiltroEstado.Todas)
    val filtro: StateFlow<FiltroEstado> = _filtro.asStateFlow()
    private var todas: List<Cita> = emptyList()

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = CitasUiState.Loading
            try {
                delay(800)
                todas = obtenerCitas()
                actualizar()
            } catch (error: Exception) {
                _uiState.value = CitasUiState.Error(error.message ?: "No se pudieron cargar las citas")
            }
        }
    }

    fun buscar(texto: String) { _busqueda.value = texto; actualizar() }
    fun filtrar(estado: FiltroEstado) { _filtro.value = estado; actualizar() }

    private fun actualizar() {
        val termino = sinTildes(_busqueda.value.trim())
        val resultado = todas.filter { cita ->
            (termino.isEmpty() || sinTildes(cita.especialidad).contains(termino) || sinTildes(cita.medico).contains(termino)) &&
                when (_filtro.value) {
                    FiltroEstado.Todas -> true
                    FiltroEstado.Programada -> cita.estado is EstadoCita.Programada
                    FiltroEstado.Atendida -> cita.estado is EstadoCita.Atendida
                    FiltroEstado.Cancelada -> cita.estado is EstadoCita.Cancelada
                }
        }
        _uiState.value = if (resultado.isEmpty()) CitasUiState.Empty else CitasUiState.Content(resultado)
    }

    private fun sinTildes(valor: String): String = valor.lowercase()
        .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')
}
