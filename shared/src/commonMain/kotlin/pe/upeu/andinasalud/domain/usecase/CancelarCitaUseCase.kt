package pe.upeu.andinasalud.domain.usecase

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.repository.CitaRepository

sealed interface ResultadoCancelacion {
    data object Exito : ResultadoCancelacion
    data class Error(val mensaje: String) : ResultadoCancelacion
}

class CancelarCitaUseCase(private val repository: CitaRepository, private val ahora: () -> Instant = { Clock.System.now() }) {
    suspend operator fun invoke(id: String): ResultadoCancelacion {
        val cita = repository.obtenerCita(id) ?: return ResultadoCancelacion.Error("Cita no encontrada")
        if (cita.estado !is EstadoCita.Programada) return ResultadoCancelacion.Error("Solo puedes cancelar citas programadas")
        if (cita.fechaHora.toInstant(TimeZone.currentSystemDefault()) - ahora() <= 24.hours) {
            return ResultadoCancelacion.Error("Solo puedes cancelar con más de 24 horas de anticipación")
        }
        repository.guardarCita(cita.copy(estado = EstadoCita.Cancelada("Cancelada por el paciente", true)))
        return ResultadoCancelacion.Exito
    }
}
