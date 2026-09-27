package pe.upeu.andinasalud

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.model.Medico
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.model.Sede
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ResultadoCancelacion
import pe.upeu.andinasalud.domain.usecase.ResultadoSolicitud
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.SolicitudCita
import pe.upeu.andinasalud.domain.usecase.ValidarCitaUseCase

class ReglasDeNegocioTest {
    private val ahoraLocal = LocalDateTime(2026, 10, 1, 9, 0)
    private val ahora = ahoraLocal.toInstant(TimeZone.currentSystemDefault())
    private val validar = ValidarCitaUseCase { ahora }
    private val paciente = Paciente("P1", "Lucía", "123", "a@b.pe", "999")

    private class Repo(val paciente: Paciente, val citas: MutableList<Cita> = mutableListOf()) : CitaRepository {
        override suspend fun obtenerCitas() = citas.toList()
        override suspend fun obtenerCita(id: String) = citas.firstOrNull { it.id == id }
        override suspend fun guardarCita(cita: Cita) {
            val index = citas.indexOfFirst { it.id == cita.id }
            if (index == -1) citas.add(cita) else citas[index] = cita
        }
        override suspend fun obtenerPaciente() = paciente
        override suspend fun obtenerSedes() = listOf(Sede("S1", "Ñaña"))
        override suspend fun obtenerEspecialidades() = listOf("Medicina General")
        override suspend fun obtenerMedicos() = listOf(Medico("M1", "Dr. Iván Rojas", "Medicina General", setOf("Ñaña")))
    }

    private fun cita(id: String, fechaHora: LocalDateTime, estado: EstadoCita = EstadoCita.Programada(false)) =
        Cita(id, paciente.id, "Medicina General", "Dr. Iván Rojas", "Ñaña", fechaHora, "Consulta general", estado)

    @Test fun rechazaPasadoYLimitesDelMotivo() {
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-01", "08:59", "Consulta general").hora)
        assertNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-01", "09:01", "Consulta general").hora)
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(9)).motivo)
        assertNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(10)).motivo)
        assertNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(200)).motivo)
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(201)).motivo)
    }

    @Test fun rechazaCuartaProgramadaYHorarioDeLaMismaHora() { runBlocking {
        val repo = Repo(paciente, mutableListOf(
            cita("1", LocalDateTime(2026, 10, 2, 10, 0)),
            cita("2", LocalDateTime(2026, 10, 3, 10, 0)),
            cita("3", LocalDateTime(2026, 10, 4, 10, 0)),
        ))
        val solicitar = SolicitarCitaUseCase(repo, validar)
        assertIs<ResultadoSolicitud.Error>(solicitar(SolicitudCita("Medicina General", "Ñaña", "2026-10-05", "11:00", "Consulta general")))
        repo.citas.removeLast()
        val duplicada = solicitar(SolicitudCita("Medicina General", "Ñaña", "2026-10-02", "10:30", "Consulta general"))
        assertIs<ResultadoSolicitud.Error>(duplicada)
        assertEquals(2, repo.citas.size)
    } }

    @Test fun cancelarExigeEstadoProgramadaYMasDe24Horas() { runBlocking {
        val repo = Repo(paciente, mutableListOf(
            cita("1", LocalDateTime(2026, 10, 2, 9, 0)),
            cita("2", LocalDateTime(2026, 10, 2, 9, 1)),
            cita("3", LocalDateTime(2026, 10, 3, 9, 0), EstadoCita.Atendida("Control")),
        ))
        val cancelar = CancelarCitaUseCase(repo) { ahora }
        assertIs<ResultadoCancelacion.Error>(cancelar("1"))
        assertIs<ResultadoCancelacion.Exito>(cancelar("2"))
        assertIs<EstadoCita.Cancelada>(repo.citas[1].estado)
        assertIs<ResultadoCancelacion.Error>(cancelar("3"))
        assertIs<ResultadoCancelacion.Error>(cancelar("2"))
    } }
}
