package pe.upeu.andinasalud.presentation.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.model.ModalidadAtencion
import pe.upeu.andinasalud.domain.usecase.ErroresSolicitud
import pe.upeu.andinasalud.domain.usecase.ResultadoSolicitud
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.SolicitudCita

data class SolicitudUiState(
    val especialidad: String = "",
    val sede: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    val modalidad: ModalidadAtencion = ModalidadAtencion.Presencial,
    val especialidades: List<String> = emptyList(),
    val sedes: List<String> = emptyList(),
    val errores: ErroresSolicitud = ErroresSolicitud(),
    val cargandoOpciones: Boolean = true,
    val enviando: Boolean = false,
    val completada: Boolean = false,
    val errorGeneral: String? = null,
)

class SolicitudViewModel(private val solicitar: SolicitarCitaUseCase, private val repository: CitaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    fun cargarOpciones() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoOpciones = true, errorGeneral = null)
            try {
                delay(800)
                _uiState.value = _uiState.value.copy(especialidades = repository.obtenerEspecialidades(), sedes = repository.obtenerSedes().map { it.nombre }, cargandoOpciones = false)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(cargandoOpciones = false, errorGeneral = error.message ?: "No se pudieron cargar las opciones")
            }
        }
    }
    fun especialidad(valor: String) { _uiState.value = _uiState.value.copy(especialidad = valor, errores = _uiState.value.errores.copy(especialidad = null)) }
    fun sede(valor: String) { _uiState.value = _uiState.value.copy(sede = valor, errores = _uiState.value.errores.copy(sede = null)) }
    fun fecha(valor: String) { _uiState.value = _uiState.value.copy(fecha = valor, errores = _uiState.value.errores.copy(fecha = null)) }
    fun hora(valor: String) { _uiState.value = _uiState.value.copy(hora = valor, errores = _uiState.value.errores.copy(hora = null)) }
    fun motivo(valor: String) { _uiState.value = _uiState.value.copy(motivo = valor, errores = _uiState.value.errores.copy(motivo = null)) }
    fun modalidad(valor: ModalidadAtencion) { _uiState.value = _uiState.value.copy(modalidad = valor) }

    fun enviar() {
        val datos = _uiState.value
        if (datos.enviando) return
        viewModelScope.launch {
            _uiState.value = datos.copy(enviando = true, errorGeneral = null)
            try {
                when (val resultado = solicitar(SolicitudCita(datos.especialidad, datos.sede, datos.fecha.trim(), datos.hora.trim(), datos.motivo, datos.modalidad))) {
                    is ResultadoSolicitud.Exito -> _uiState.value = _uiState.value.copy(enviando = false, completada = true)
                    is ResultadoSolicitud.Error -> _uiState.value = _uiState.value.copy(enviando = false, errores = resultado.errores, errorGeneral = resultado.mensaje)
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(enviando = false, errorGeneral = error.message ?: "No se pudo solicitar la cita")
            }
        }
    }
    fun reiniciar() { _uiState.value = SolicitudUiState(especialidades = _uiState.value.especialidades, sedes = _uiState.value.sedes, cargandoOpciones = false) }
}
