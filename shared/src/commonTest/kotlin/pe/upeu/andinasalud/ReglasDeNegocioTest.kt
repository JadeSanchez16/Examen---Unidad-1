package pe.upeu.andinasalud

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.EstadoCita
import pe.upeu.andinasalud.domain.model.Medico
import pe.upeu.andinasalud.domain.model.ModalidadAtencion
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.model.Sede
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ResultadoCancelacion
import pe.upeu.andinasalud.domain.usecase.ResultadoSolicitud
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.SolicitudCita
import pe.upeu.andinasalud.domain.usecase.ValidarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ReprogramarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ResultadoReprogramacion

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
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00:45", "Consulta general").hora)
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(9)).motivo)
        assertNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(10)).motivo)
        assertNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(200)).motivo)
        assertNotNull(validar.validarCampos("Medicina General", "Ñaña", "2026-10-02", "10:00", "a".repeat(201)).motivo)
    }

    @Test fun rechazaCuartaProgramadaYHorarioExactamenteDuplicado() { runBlocking {
        val repo = Repo(paciente, mutableListOf(
            cita("1", LocalDateTime(2026, 10, 2, 10, 0)),
            cita("2", LocalDateTime(2026, 10, 3, 10, 0)),
            cita("3", LocalDateTime(2026, 10, 4, 10, 0)),
        ))
        val solicitar = SolicitarCitaUseCase(repo, validar)
        assertIs<ResultadoSolicitud.Error>(solicitar(SolicitudCita("Medicina General", "Ñaña", "2026-10-05", "11:00", "Consulta general")))
        assertFalse(validar.puedeSolicitar(repo.citas, paciente.id))
        repo.citas.removeLast()
        val duplicada = solicitar(SolicitudCita("Medicina General", "Ñaña", "2026-10-02", "10:00", "Consulta general"))
        assertIs<ResultadoSolicitud.Error>(duplicada)
        assertEquals(2, repo.citas.size)
        assertEquals(2, validar.contarProgramadas(repo.citas, paciente.id))
        assertTrue(validar.puedeSolicitar(repo.citas, paciente.id))
        assertIs<ResultadoSolicitud.Exito>(solicitar(SolicitudCita("Medicina General", "Ñaña", "2026-10-02", "10:30", "Consulta general")))
        assertEquals(3, repo.citas.size)
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

    @Test fun solicitudConservaModalidadElegida() { runBlocking {
        val repo = Repo(paciente)
        val resultado = SolicitarCitaUseCase(repo, validar)(
            SolicitudCita("Medicina General", "Ñaña", "2026-10-05", "11:00", "Consulta general", ModalidadAtencion.Teleconsulta)
        )
        val exito = assertIs<ResultadoSolicitud.Exito>(resultado)
        assertEquals(ModalidadAtencion.Teleconsulta, exito.cita.modalidad)
        assertEquals(ModalidadAtencion.Teleconsulta, repo.citas.single().modalidad)
    } }

    @Test fun reprogramarReutilizaValidacionesYRegistraCambio() { runBlocking {
        val inicial = LocalDateTime(2026, 10, 2, 10, 0)
        val repo = Repo(paciente, mutableListOf(
            cita("1", inicial),
            cita("2", LocalDateTime(2026, 10, 3, 11, 0)),
            cita("3", LocalDateTime(2026, 10, 4, 12, 0)),
            cita("4", LocalDateTime(2026, 9, 30, 10, 0), EstadoCita.Atendida("Control")),
        ))
        val reprogramar = ReprogramarCitaUseCase(repo, validar)
        assertIs<ResultadoReprogramacion.Error>(reprogramar("4", "2026-10-06", "14:00"))
        assertIs<ResultadoReprogramacion.Error>(reprogramar("1", "2026-09-30", "14:00"))
        assertIs<ResultadoReprogramacion.Error>(reprogramar("1", "2026-10-03", "11:00"))
        assertIs<ResultadoReprogramacion.Error>(reprogramar("1", "2026-10-02", "10:00"))
        val exito = assertIs<ResultadoReprogramacion.Exito>(reprogramar("1", "2026-10-06", "14:00"))
        assertEquals(3, validar.contarProgramadas(repo.citas, paciente.id))
        assertEquals(inicial, exito.cita.reprogramaciones.single().anterior)
        assertEquals(LocalDateTime(2026, 10, 6, 14, 0), repo.citas.first().fechaHora)
    } }

    @Test fun reprogramarNoAceptaElMismoMinutoAunqueExistanSegundosOcultos() { runBlocking {
        val repo = Repo(paciente, mutableListOf(cita("1", LocalDateTime(2026, 10, 2, 10, 0, 45))))
        val resultado = ReprogramarCitaUseCase(repo, validar)("1", "2026-10-02", "10:00")
        assertIs<ResultadoReprogramacion.Error>(resultado)
        assertTrue(repo.citas.single().reprogramaciones.isEmpty())
    } }
}
