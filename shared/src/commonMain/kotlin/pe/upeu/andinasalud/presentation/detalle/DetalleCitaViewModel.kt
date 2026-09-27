package pe.upeu.andinasalud.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.upeu.andinasalud.domain.usecase.ResultadoCancelacion

sealed interface DetalleUiState {
    data object Loading : DetalleUiState
    data class Content(val cita: Cita, val mensaje: String? = null, val canceladaAhora: Boolean = false) : DetalleUiState
    data object Empty : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}

class DetalleCitaViewModel(private val obtener: ObtenerCitasUseCase, private val cancelar: CancelarCitaUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Loading)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Loading
            try {
                delay(800)
                val cita = obtener.porId(id)
                _uiState.value = if (cita == null) DetalleUiState.Empty else DetalleUiState.Content(cita)
            } catch (error: Exception) {
                _uiState.value = DetalleUiState.Error(error.message ?: "No se pudo cargar el detalle")
            }
        }
    }

    fun cancelar(id: String) {
        viewModelScope.launch {
            try {
                val resultado = cancelar.invoke(id)
                val cita = obtener.porId(id)
                _uiState.value = when {
                    cita == null -> DetalleUiState.Empty
                    resultado is ResultadoCancelacion.Error -> DetalleUiState.Content(cita, resultado.mensaje)
                    else -> DetalleUiState.Content(cita, "Cita cancelada", canceladaAhora = true)
                }
            } catch (error: Exception) {
                _uiState.value = DetalleUiState.Error(error.message ?: "No se pudo cancelar la cita")
            }
        }
    }
}
