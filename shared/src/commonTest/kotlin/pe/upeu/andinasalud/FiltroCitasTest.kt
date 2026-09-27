package pe.upeu.andinasalud

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
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
}
