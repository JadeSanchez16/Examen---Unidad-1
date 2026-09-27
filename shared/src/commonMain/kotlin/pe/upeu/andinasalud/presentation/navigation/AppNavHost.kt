package pe.upeu.andinasalud.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ObtenerCitasUseCase
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase
import pe.upeu.andinasalud.presentation.citas.CitasScreen
import pe.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.upeu.andinasalud.presentation.detalle.DetalleCitaScreen
import pe.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.upeu.andinasalud.presentation.inicio.InicioScreen
import pe.upeu.andinasalud.presentation.inicio.InicioUiState
import pe.upeu.andinasalud.presentation.inicio.InicioViewModel
import pe.upeu.andinasalud.presentation.perfil.PerfilScreen
import pe.upeu.andinasalud.presentation.perfil.AjustesScreen
import pe.upeu.andinasalud.presentation.perfil.PerfilUiState
import pe.upeu.andinasalud.presentation.perfil.PerfilViewModel
import pe.upeu.andinasalud.presentation.solicitud.SolicitudScreen
import pe.upeu.andinasalud.presentation.solicitud.SolicitudViewModel

private fun NavHostController.irPrincipal(destino: String) {
    navigate(destino) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(repository: CitaRepository, oscuro: Boolean, onTema: (Boolean) -> Unit) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val obtener: ObtenerCitasUseCase = koinInject()
    val cancelar: CancelarCitaUseCase = koinInject()
    val solicitar: SolicitarCitaUseCase = koinInject()
    val inicioVm = remember { InicioViewModel(repository, obtener) }
    val citasVm = remember { CitasViewModel(obtener) }
    val detalleVm = remember { DetalleCitaViewModel(obtener, cancelar) }
    val solicitudVm = remember { SolicitudViewModel(solicitar, repository) }
    val perfilVm = remember { PerfilViewModel(repository) }

    Scaffold(bottomBar = {
        NavigationBar {
            listOf(Destinos.INICIO to "Inicio", Destinos.CITAS to "Citas", Destinos.PERFIL to "Perfil").forEach { (destino, etiqueta) ->
                NavigationBarItem(selected = ruta == destino, onClick = { nav.irPrincipal(destino) }, icon = {
                    Icon(when (destino) { Destinos.INICIO -> Icons.Default.Home; Destinos.CITAS -> Icons.Default.DateRange; else -> Icons.Default.Person }, contentDescription = null)
                }, label = { Text(etiqueta) })
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = Destinos.INICIO, modifier = Modifier.fillMaxSize().padding(padding)) {
            composable(Destinos.INICIO) {
                LaunchedEffect(Unit) { inicioVm.cargar() }
                when (val state = inicioVm.uiState.collectAsState().value) {
                    InicioUiState.Loading -> Text("Cargando inicio…", modifier = Modifier.padding(padding))
                    is InicioUiState.Error -> {
                        Text(state.mensaje)
                        androidx.compose.material3.TextButton(onClick = inicioVm::cargar) { Text("Reintentar") }
                    }
                    is InicioUiState.Content -> InicioScreen(state.paciente, state.proxima, { nav.irPrincipal(Destinos.CITAS) }, { nav.navigate(Destinos.SOLICITUD) }, { nav.navigate(Destinos.detalle(it)) }, inicioVm::cargar, null)
                }
            }
            composable(Destinos.CITAS) {
                LaunchedEffect(Unit) { citasVm.cargar() }
                CitasScreen(citasVm) { nav.navigate(Destinos.detalle(it)) }
            }
            composable(Destinos.PERFIL) {
                LaunchedEffect(Unit) { perfilVm.cargar() }
                when (val state = perfilVm.uiState.collectAsState().value) {
                    PerfilUiState.Loading -> Text("Cargando perfil…")
                    is PerfilUiState.Error -> {
                        Column {
                            Text(state.mensaje)
                            androidx.compose.material3.TextButton(onClick = perfilVm::cargar) { Text("Reintentar") }
                        }
                    }
                    is PerfilUiState.Content -> PerfilScreen(state.paciente) { nav.navigate(Destinos.AJUSTES) }
                }
            }
            composable(Destinos.AJUSTES) { AjustesScreen(oscuro, onTema) }
            composable(Destinos.SOLICITUD) {
                LaunchedEffect(Unit) { solicitudVm.cargarOpciones() }
                SolicitudScreen(solicitudVm) { nav.irPrincipal(Destinos.CITAS); citasVm.cargar() }
            }
            composable(Destinos.DETALLE) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                LaunchedEffect(id) { detalleVm.cargar(id) }
                DetalleCitaScreen(id, detalleVm)
            }
        }
    }
}
