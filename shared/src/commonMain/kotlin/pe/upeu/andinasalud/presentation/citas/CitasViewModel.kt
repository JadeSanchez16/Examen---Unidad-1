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
import pe.upeu.andinasalud.domain.usecase.FiltrarCitasUseCase
import pe.upeu.andinasalud.domain.usecase.FiltroEstado

class CitasViewModel(private val obtenerCitas: ObtenerCitasUseCase, private val filtrarCitas: FiltrarCitasUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<CitasUiState>(CitasUiState.Loading)
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()
    private val _filtro = MutableStateFlow(FiltroEstado.Todas)
    val filtro: StateFlow<FiltroEstado> = _filtro.asStateFlow()
    private val _soloHoy = MutableStateFlow(false)
    val soloHoy: StateFlow<Boolean> = _soloHoy.asStateFlow()
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
    fun seleccionarHoy(activo: Boolean) { _soloHoy.value = activo; actualizar() }

    private fun actualizar() {
        val resultado = filtrarCitas.filtrar(todas, _filtro.value, _busqueda.value, _soloHoy.value)
        _uiState.value = if (resultado.isEmpty()) CitasUiState.Empty else CitasUiState.Content(resultado)
    }
}
