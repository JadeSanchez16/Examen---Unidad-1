package pe.upeu.andinasalud.presentation.reprogramacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.usecase.ErroresSolicitud
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.upeu.andinasalud.domain.usecase.ReprogramarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ResultadoReprogramacion

data class ReprogramacionUiState(
    val fecha: String = "",
    val hora: String = "",
    val errores: ErroresSolicitud = ErroresSolicitud(),
    val cargando: Boolean = true,
    val enviando: Boolean = false,
    val completada: Boolean = false,
    val errorGeneral: String? = null,
)

class ReprogramacionViewModel(private val obtener: ObtenerCitasUseCase, private val reprogramar: ReprogramarCitaUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(ReprogramacionUiState())
    val uiState: StateFlow<ReprogramacionUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = ReprogramacionUiState()
            try {
                delay(800)
                val cita = obtener.porId(id)
                _uiState.value = if (cita == null) ReprogramacionUiState(cargando = false, errorGeneral = "Cita no encontrada")
                else ReprogramacionUiState(fecha = cita.fechaHora.date.toString(), hora = cita.fechaHora.time.toString().take(5), cargando = false)
            } catch (error: Exception) {
                _uiState.value = ReprogramacionUiState(cargando = false, errorGeneral = error.message ?: "No se pudo cargar la cita")
            }
        }
    }
    fun fecha(valor: String) { _uiState.value = _uiState.value.copy(fecha = valor, errores = _uiState.value.errores.copy(fecha = null)) }
    fun hora(valor: String) { _uiState.value = _uiState.value.copy(hora = valor, errores = _uiState.value.errores.copy(hora = null)) }

    fun enviar(id: String) {
        val actual = _uiState.value
        if (actual.enviando || actual.cargando) return
        viewModelScope.launch {
            _uiState.value = actual.copy(enviando = true, errorGeneral = null)
            try {
                when (val resultado = reprogramar(id, actual.fecha.trim(), actual.hora.trim())) {
                    is ResultadoReprogramacion.Exito -> _uiState.value = _uiState.value.copy(enviando = false, completada = true)
                    is ResultadoReprogramacion.Error -> _uiState.value = _uiState.value.copy(enviando = false, errores = resultado.errores, errorGeneral = resultado.mensaje)
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(enviando = false, errorGeneral = error.message ?: "No se pudo reprogramar")
            }
        }
    }
}
