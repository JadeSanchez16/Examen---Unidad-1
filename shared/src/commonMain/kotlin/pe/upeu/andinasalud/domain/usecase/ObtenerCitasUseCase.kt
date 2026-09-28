package pe.upeu.andinasalud.domain.usecase

import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.domain.repository.CitaRepository

class ObtenerCitasUseCase(private val repository: CitaRepository, private val filtrar: FiltrarCitasUseCase) {
    suspend operator fun invoke(): List<Cita> = filtrar.filtrar(repository.obtenerCitas(), FiltroEstado.Todas, "", false)
    suspend fun porId(id: String): Cita? = repository.obtenerCita(id)
}
