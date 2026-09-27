package pe.upeu.andinasalud.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pe.upeu.andinasalud.data.repository.CitaRepositoryFake
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ValidarCitaUseCase

val appModule = module {
    singleOf(::CitaRepositoryFake) bind CitaRepository::class
    singleOf(::ValidarCitaUseCase)
    singleOf(::ObtenerCitasUseCase)
    singleOf(::SolicitarCitaUseCase)
    singleOf(::CancelarCitaUseCase)
}

fun inicializarKoin() = startKoin { modules(appModule) }
