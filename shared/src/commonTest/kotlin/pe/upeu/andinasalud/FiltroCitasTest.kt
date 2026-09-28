package pe.upeu.andinasalud

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.usecase.FiltrarCitasUseCase
import pe.upeu.andinasalud.domain.usecase.FiltroEstado

class FiltroCitasTest {
    private val filtro = FiltrarCitasUseCase { LocalDate(2026, 10, 1) }
    private val citas = listOf(
        Cita("1", "P1", "Pediatría", "Dra. Núñez", "Ñaña", LocalDateTime(2026, 10, 1, 10, 0), "Consulta general", EstadoCita.Programada(false)),
        Cita("2", "P1", "Pediatría", "Dra. Núñez", "Ñaña", LocalDateTime(2026, 10, 2, 10, 0), "Consulta general", EstadoCita.Programada(false)),
        Cita("3", "P1", "Psicología", "Ps. Tapia", "Ñaña", LocalDateTime(2026, 10, 1, 11, 0), "Consulta general", EstadoCita.Atendida("Control")),
    )

    @Test fun hoySeCombinaConEstadoYBusquedaSinTildes() {
        assertEquals(listOf("1", "3"), filtro.filtrar(citas, FiltroEstado.Todas, "", true).map { it.id })
        assertEquals(listOf("1"), filtro.filtrar(citas, FiltroEstado.Programada, "PEDIATRIA", true).map { it.id })
        assertEquals(listOf("1"), filtro.filtrar(citas, FiltroEstado.Programada, "nunez", true).map { it.id })
        assertEquals(listOf("1", "2"), filtro.filtrar(citas, FiltroEstado.Programada, "", false).map { it.id })
        assertEquals(emptyList(), filtro.filtrar(citas, FiltroEstado.Cancelada, "", true))
    }

    @Test fun busquedaOrdenadaIgnoraMayusculasYTildes() {
        assertEquals(listOf("1", "2"), filtro.filtrar(citas.reversed(), FiltroEstado.Todas, "PEDIATRIA", false).map { it.id })
        assertEquals(listOf("1", "2"), filtro.filtrar(citas.reversed(), FiltroEstado.Todas, "NUNEZ", false).map { it.id })
    }

    @Test fun ordenaPorCercaniaAlMomentoActual() {
        val ahora = LocalDateTime(2026, 10, 1, 9, 0).toInstant(TimeZone.currentSystemDefault())
        val filtro = FiltrarCitasUseCase(ahora = { ahora })
        val pasada = citas[0].copy(fechaHora = LocalDateTime(2026, 9, 20, 10, 0))
        assertEquals(listOf("3", "2", "1"), filtro.filtrar(listOf(pasada, citas[1], citas[2]), FiltroEstado.Todas, "", false).map { it.id })
    }
}
