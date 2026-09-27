package pe.upeu.andinasalud.domain.model

import kotlinx.datetime.LocalDateTime

data class Cita(
    val id: String,
    val pacienteId: String,
    val especialidad: String,
    val medico: String,
    val sede: String,
    val fechaHora: LocalDateTime,
    val motivo: String,
    val estado: EstadoCita,
    val modalidad: ModalidadAtencion = ModalidadAtencion.Presencial,
    val reprogramaciones: List<RegistroReprogramacion> = emptyList(),
)
