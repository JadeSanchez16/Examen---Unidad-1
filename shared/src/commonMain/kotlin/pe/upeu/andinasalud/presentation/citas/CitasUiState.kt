package pe.upeu.andinasalud.presentation.citas

import pe.upeu.andinasalud.domain.model.Cita

sealed interface CitasUiState {
    data object Loading : CitasUiState
    data class Content(val citas: List<Cita>) : CitasUiState
    data object Empty : CitasUiState
    data class Error(val mensaje: String) : CitasUiState
}
