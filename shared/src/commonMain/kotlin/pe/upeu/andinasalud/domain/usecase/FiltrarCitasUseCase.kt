package pe.upeu.andinasalud.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita

enum class FiltroEstado { Todas, Programada, Atendida, Cancelada }

class FiltrarCitasUseCase(private val hoy: () -> LocalDate = { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }) {
    fun filtrar(citas: List<Cita>, estado: FiltroEstado, busqueda: String, soloHoy: Boolean): List<Cita> {
        val termino = normalizar(busqueda.trim())
        val fechaHoy = if (soloHoy) hoy() else null
        return citas.filter { cita ->
            (fechaHoy == null || cita.fechaHora.date == fechaHoy) &&
                (termino.isEmpty() || normalizar(cita.especialidad).contains(termino) || normalizar(cita.medico).contains(termino)) &&
                when (estado) {
                    FiltroEstado.Todas -> true
                    FiltroEstado.Programada -> cita.estado is EstadoCita.Programada
                    FiltroEstado.Atendida -> cita.estado is EstadoCita.Atendida
                    FiltroEstado.Cancelada -> cita.estado is EstadoCita.Cancelada
                }
        }.sortedBy { it.fechaHora }
    }

    private fun normalizar(valor: String): String = valor.lowercase()
        .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')
}
