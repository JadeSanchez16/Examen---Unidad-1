package pe.upeu.andinasalud.domain.usecase

import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.model.RegistroReprogramacion
import pe.upeu.andinasalud.domain.repository.CitaRepository

sealed interface ResultadoReprogramacion {
    data class Exito(val cita: Cita) : ResultadoReprogramacion
    data class Error(val errores: ErroresSolicitud = ErroresSolicitud(), val mensaje: String? = null) : ResultadoReprogramacion
}

class ReprogramarCitaUseCase(private val repository: CitaRepository, private val validar: ValidarCitaUseCase) {
    suspend operator fun invoke(id: String, fecha: String, hora: String): ResultadoReprogramacion {
        val cita = repository.obtenerCita(id) ?: return ResultadoReprogramacion.Error(mensaje = "Cita no encontrada")
        if (cita.estado !is EstadoCita.Programada) return ResultadoReprogramacion.Error(mensaje = "Solo puedes reprogramar citas programadas")
        val errores = validar.validarCampos(cita.especialidad, cita.sede, fecha, hora, cita.motivo)
        if (errores.hayErrores) return ResultadoReprogramacion.Error(errores)
        val nueva = validar.parsear(fecha, hora) ?: return ResultadoReprogramacion.Error(ErroresSolicitud(fecha = "Fecha u hora no válida"))
        if (cita.fechaHora.date == nueva.date && cita.fechaHora.hour == nueva.hour && cita.fechaHora.minute == nueva.minute) {
            return ResultadoReprogramacion.Error(ErroresSolicitud(hora = "Elige un horario distinto"))
        }
        val conflicto = validar.validarHorario(repository.obtenerCitas(), cita.pacienteId, nueva, excluirId = cita.id)
        if (conflicto != null) return ResultadoReprogramacion.Error(ErroresSolicitud(hora = conflicto))
        val actualizada = cita.copy(
            fechaHora = nueva,
            reprogramaciones = cita.reprogramaciones + RegistroReprogramacion(cita.fechaHora, nueva),
        )
        repository.guardarCita(actualizada)
        return ResultadoReprogramacion.Exito(actualizada)
    }
}
