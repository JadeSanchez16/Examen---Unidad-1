package pe.upeu.andinasalud.domain.usecase

import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.repository.CitaRepository

data class SolicitudCita(val especialidad: String, val sede: String, val fecha: String, val hora: String, val motivo: String)

sealed interface ResultadoSolicitud {
    data class Exito(val cita: Cita) : ResultadoSolicitud
    data class Error(val errores: ErroresSolicitud, val mensaje: String? = null) : ResultadoSolicitud
}

class SolicitarCitaUseCase(private val repository: CitaRepository, private val validar: ValidarCitaUseCase) {
    suspend operator fun invoke(solicitud: SolicitudCita): ResultadoSolicitud {
        val errores = validar.validarCampos(solicitud.especialidad, solicitud.sede, solicitud.fecha, solicitud.hora, solicitud.motivo)
        if (errores.hayErrores) return ResultadoSolicitud.Error(errores)
        val fechaHora = validar.parsear(solicitud.fecha, solicitud.hora) ?: return ResultadoSolicitud.Error(ErroresSolicitud(fecha = "Fecha no válida"))
        val especialidades = repository.obtenerEspecialidades()
        val sedes = repository.obtenerSedes()
        if (solicitud.especialidad !in especialidades) return ResultadoSolicitud.Error(ErroresSolicitud(especialidad = "Especialidad no válida"))
        if (sedes.none { it.nombre == solicitud.sede }) return ResultadoSolicitud.Error(ErroresSolicitud(sede = "Sede no válida"))
        val paciente = repository.obtenerPaciente()
        val citas = repository.obtenerCitas()
        val conflicto = validar.validarDisponibilidad(citas, paciente.id, fechaHora)
        if (conflicto != null) return ResultadoSolicitud.Error(ErroresSolicitud(hora = conflicto))
        val medico = repository.obtenerMedicos().firstOrNull { it.especialidad == solicitud.especialidad && solicitud.sede in it.sedes }
            ?: return ResultadoSolicitud.Error(ErroresSolicitud(sede = "No hay médico disponible en esta sede"))
        val siguienteId = (citas.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
        val cita = Cita(siguienteId.toString(), paciente.id, solicitud.especialidad, medico.nombre, solicitud.sede, fechaHora, solicitud.motivo, EstadoCita.Programada(false))
        repository.guardarCita(cita)
        return ResultadoSolicitud.Exito(cita)
    }
}
