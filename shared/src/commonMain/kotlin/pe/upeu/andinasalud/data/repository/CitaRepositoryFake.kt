package pe.upeu.andinasalud.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.upeu.andinasalud.data.local.CitasSimuladas
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.model.Medico
import pe.upeu.andinasalud.domain.model.Paciente
import pe.upeu.andinasalud.domain.model.Sede
import pe.upeu.andinasalud.domain.repository.CitaRepository

class CitaRepositoryFake : CitaRepository {
    private val mutex = Mutex()
    private val citas = CitasSimuladas.citas().toMutableList()

    override suspend fun obtenerCitas(): List<Cita> = mutex.withLock { citas.toList() }
    override suspend fun obtenerCita(id: String): Cita? = mutex.withLock { citas.firstOrNull { it.id == id } }
    override suspend fun guardarCita(cita: Cita) {
        mutex.withLock {
            val index = citas.indexOfFirst { it.id == cita.id }
            if (index < 0) citas.add(cita) else citas[index] = cita
        }
    }
    override suspend fun obtenerPaciente(): Paciente = CitasSimuladas.paciente
    override suspend fun obtenerSedes(): List<Sede> = CitasSimuladas.sedes
    override suspend fun obtenerEspecialidades(): List<String> = CitasSimuladas.especialidades
    override suspend fun obtenerMedicos(): List<Medico> = CitasSimuladas.medicos
}
