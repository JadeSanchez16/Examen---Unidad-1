package pe.upeu.andinasalud

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import pe.upeu.andinasalud.data.local.CitasSimuladas
import pe.upeu.andinasalud.domain.model.EstadoCita

class DatosSimuladosTest {
    @Test fun catalogoYCitasCumplenLosMinimosDelExamen() {
        val sedes = CitasSimuladas.sedes.map { it.nombre }.toSet()
        val especialidades = CitasSimuladas.especialidades
        val medicos = CitasSimuladas.medicos
        val citas = CitasSimuladas.citas()
        val ahora = Clock.System.now()

        assertEquals(setOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita"), sedes)
        assertEquals(setOf("Medicina General", "Odontología", "Pediatría", "Nutrición", "Psicología"), especialidades.toSet())
        especialidades.forEach { especialidad ->
            assertTrue(medicos.count { it.especialidad == especialidad } >= 2)
        }
        assertTrue(medicos.all { it.sedes.isNotEmpty() && it.sedes.all { sede -> sede in sedes } })
        assertTrue(citas.size >= 6)
        assertEquals(3, citas.count { it.estado is EstadoCita.Programada })
        assertEquals(2, citas.count { it.estado is EstadoCita.Atendida })
        assertEquals(1, citas.count { it.estado is EstadoCita.Cancelada })
        assertTrue(citas.filter { it.estado is EstadoCita.Programada }.all {
            it.fechaHora.toInstant(TimeZone.currentSystemDefault()) > ahora
        })
    }
}
