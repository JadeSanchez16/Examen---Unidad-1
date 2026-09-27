package pe.upeu.andinasalud.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.usecase.ObtenerResumenCitasUseCase
import pe.upeu.andinasalud.domain.usecase.ResumenCitas

sealed interface ResumenUiState {
    data object Loading : ResumenUiState
    data class Content(val resumen: ResumenCitas) : ResumenUiState
    data class Error(val mensaje: String) : ResumenUiState
}

class ResumenCitasViewModel(private val obtenerResumen: ObtenerResumenCitasUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<ResumenUiState>(ResumenUiState.Loading)
    val uiState: StateFlow<ResumenUiState> = _uiState.asStateFlow()
    fun cargar() {
        viewModelScope.launch {
            try {
                _uiState.value = ResumenUiState.Content(obtenerResumen())
            } catch (error: Exception) {
                _uiState.value = ResumenUiState.Error(error.message ?: "No se pudo consultar el cupo")
            }
        }
    }
}
