package pe.upeu.andinasalud.data.local

import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.model.Medico
import pe.upeu.andinasalud.domain.model.ModalidadAtencion
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.model.Sede

object CitasSimuladas {
    val paciente = Paciente("P-0417", "Jade Sanchez", "61098438", "jade.sanchez@gmail.com", "997 652 798")
    val sedes = listOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita").mapIndexed { index, nombre -> Sede("S${index + 1}", nombre) }
    val especialidades = listOf("Medicina General", "Odontología", "Pediatría", "Nutrición", "Psicología")
    val medicos = listOf(
        Medico("M1", "Dr. Iván Rojas", "Medicina General", setOf("Ñaña", "Chosica")),
        Medico("M2", "Dra. Elena Paredes", "Medicina General", setOf("Santa Anita", "Chaclacayo")),
        Medico("M3", "Dra. Rosa Flores", "Odontología", setOf("Chosica", "Ñaña")),
        Medico("M4", "Dr. Daniel Torres", "Odontología", setOf("Santa Anita", "Chaclacayo")),
        Medico("M5", "Dra. Carla Núñez", "Pediatría", setOf("Chaclacayo", "Ñaña")),
        Medico("M6", "Dr. José Medina", "Pediatría", setOf("Santa Anita", "Chosica")),
        Medico("M7", "Lic. Ana Bermúdez", "Nutrición", setOf("Santa Anita", "Ñaña")),
        Medico("M8", "Lic. Paola Díaz", "Nutrición", setOf("Chosica", "Chaclacayo")),
        Medico("M9", "Ps. Luis Tapia", "Psicología", setOf("Ñaña", "Chosica")),
        Medico("M10", "Ps. María Salazar", "Psicología", setOf("Santa Anita", "Chaclacayo")),
    )

    fun citas(): List<Cita> {
        val ahora = Clock.System.now()
        val zona = TimeZone.currentSystemDefault()
        fun fecha(dias: Int, hora: Int, minuto: Int): LocalDateTime =
            LocalDateTime((ahora + dias.days).toLocalDateTime(zona).date, LocalTime(hora, minuto))
        return listOf(
            Cita("1", paciente.id, "Medicina General", "Dr. Iván Rojas", "Ñaña", fecha(3, 9, 0), "Consulta de seguimiento", EstadoCita.Programada(true)),
            Cita("2", paciente.id, "Odontología", "Dra. Rosa Flores", "Chosica", fecha(6, 16, 30), "Revisión dental anual", EstadoCita.Programada(false)),
            Cita("3", paciente.id, "Nutrición", "Lic. Ana Bermúdez", "Santa Anita", fecha(9, 11, 15), "Evaluación nutricional", EstadoCita.Programada(true), ModalidadAtencion.Teleconsulta),
            Cita("4", paciente.id, "Pediatría", "Dra. Carla Núñez", "Chaclacayo", fecha(-28, 8, 45), "Control pediátrico", EstadoCita.Atendida("Control en tres meses")),
            Cita("5", paciente.id, "Psicología", "Ps. Luis Tapia", "Ñaña", fecha(-20, 15, 0), "Consulta de seguimiento", EstadoCita.Atendida("Continuar sesiones quincenales"), ModalidadAtencion.Teleconsulta),
            Cita("6", paciente.id, "Medicina General", "Dr. Iván Rojas", "Chosica", fecha(-14, 10, 30), "Control general", EstadoCita.Cancelada("Viaje del paciente", true)),
        )
    }
}
