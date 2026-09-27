package pe.upeu.andinasalud.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.repository.CitaRepository

sealed interface PerfilUiState {
    data object Loading : PerfilUiState
    data class Content(val paciente: Paciente) : PerfilUiState
    data class Error(val mensaje: String) : PerfilUiState
}

class PerfilViewModel(private val repository: CitaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                delay(800)
                _uiState.value = PerfilUiState.Content(repository.obtenerPaciente())
            } catch (error: Exception) {
                _uiState.value = PerfilUiState.Error(error.message ?: "No se pudo cargar el perfil")
            }
        }
    }
}
