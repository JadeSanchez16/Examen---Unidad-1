package pe.upeu.andinasalud.domain.usecase

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Instant
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita

data class ErroresSolicitud(
    val especialidad: String? = null,
    val sede: String? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val motivo: String? = null,
) {
    val hayErrores: Boolean get() = listOf(especialidad, sede, fecha, hora, motivo).any { it != null }
}

class ValidarCitaUseCase(private val ahora: () -> Instant = { Clock.System.now() }) {
    fun contarProgramadas(citas: List<Cita>, pacienteId: String): Int =
        citas.count { it.pacienteId == pacienteId && it.estado is EstadoCita.Programada }

    fun puedeSolicitar(citas: List<Cita>, pacienteId: String): Boolean = contarProgramadas(citas, pacienteId) < 3

    fun validarCampos(especialidad: String, sede: String, fecha: String, hora: String, motivo: String): ErroresSolicitud {
        val fechaValida = try { LocalDate.parse(fecha) } catch (_: IllegalArgumentException) { null }
        val horaValida = try { LocalTime.parse(hora) } catch (_: IllegalArgumentException) { null }
        val fechaHora = if (fechaValida != null && horaValida != null) parsear(fecha, hora) else null
        val fechaError = when {
            fecha.isBlank() -> "Selecciona una fecha"
            fechaValida == null -> "Fecha no válida"
            else -> null
        }
        val horaError = when {
            hora.isBlank() -> "Selecciona una hora"
            horaValida == null -> "Hora no válida"
            fechaHora != null && fechaHora.toInstant(TimeZone.currentSystemDefault()) <= ahora() -> "Elige una fecha y hora futuras"
            else -> null
        }
        return ErroresSolicitud(
            especialidad = if (especialidad.isBlank()) "Selecciona una especialidad" else null,
            sede = if (sede.isBlank()) "Selecciona una sede" else null,
            fecha = fechaError,
            hora = horaError,
            motivo = if (motivo.length !in 10..200) "El motivo debe tener entre 10 y 200 caracteres" else null,
        )
    }

    fun validarDisponibilidad(citas: List<Cita>, pacienteId: String, fechaHora: LocalDateTime): String? {
        val programadas = citas.filter { it.pacienteId == pacienteId && it.estado is EstadoCita.Programada }
        if (!puedeSolicitar(citas, pacienteId)) return "Ya tienes tres citas programadas"
        if (programadas.any { it.fechaHora.date == fechaHora.date && it.fechaHora.hour == fechaHora.hour }) {
            return "Ya tienes una cita programada en ese horario"
        }
        return null
    }

    fun parsear(fecha: String, hora: String): LocalDateTime? = try {
        LocalDateTime.parse("${fecha}T${hora}:00")
    } catch (_: IllegalArgumentException) {
        null
    }
}
