package pe.upeu.andinasalud.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase

sealed interface InicioUiState {
    data object Loading : InicioUiState
    data class Empty(val paciente: Paciente) : InicioUiState
    data class Content(val paciente: Paciente, val proxima: Cita?) : InicioUiState
    data class Error(val mensaje: String) : InicioUiState
}

class InicioViewModel(private val repository: CitaRepository, private val obtener: ObtenerCitasUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<InicioUiState>(InicioUiState.Loading)
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = InicioUiState.Loading
            try {
                delay(800)
                val paciente = repository.obtenerPaciente()
                val proxima = obtener().firstOrNull { it.estado is EstadoCita.Programada }
                _uiState.value = if (proxima == null) InicioUiState.Empty(paciente) else InicioUiState.Content(paciente, proxima)
            } catch (error: Exception) {
                _uiState.value = InicioUiState.Error(error.message ?: "No se pudo cargar el inicio")
            }
        }
    }
}
