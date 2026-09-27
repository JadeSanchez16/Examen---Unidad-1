package pe.upeu.andinasalud.domain.usecase

import pe.upeu.andinasalud.domain.repository.CitaRepository

data class ResumenCitas(val programadas: Int, val puedeSolicitar: Boolean)

class ObtenerResumenCitasUseCase(private val repository: CitaRepository, private val validar: ValidarCitaUseCase) {
    suspend operator fun invoke(): ResumenCitas {
        val paciente = repository.obtenerPaciente()
        val citas = repository.obtenerCitas()
        return ResumenCitas(
            programadas = validar.contarProgramadas(citas, paciente.id),
            puedeSolicitar = validar.puedeSolicitar(citas, paciente.id),
        )
    }
}
