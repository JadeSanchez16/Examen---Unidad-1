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
import pe.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.upeu.andinasalud.presentation.inicio.InicioViewModel
import pe.upeu.andinasalud.presentation.perfil.PerfilViewModel
import pe.upeu.andinasalud.presentation.solicitud.SolicitudViewModel

val appModule = module {
    singleOf(::CitaRepositoryFake) bind CitaRepository::class
    single { ValidarCitaUseCase() }
    singleOf(::ObtenerCitasUseCase)
    singleOf(::SolicitarCitaUseCase)
    single { CancelarCitaUseCase(get()) }
    singleOf(::InicioViewModel)
    singleOf(::CitasViewModel)
    singleOf(::DetalleCitaViewModel)
    singleOf(::SolicitudViewModel)
    singleOf(::PerfilViewModel)
}

fun inicializarKoin() = startKoin { modules(appModule) }
