package pe.upeu.andinasalud

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.dsl.koinApplication
import pe.upeu.andinasalud.di.appModule
import pe.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.upeu.andinasalud.presentation.inicio.InicioViewModel
import pe.upeu.andinasalud.presentation.perfil.PerfilViewModel
import pe.upeu.andinasalud.presentation.solicitud.SolicitudViewModel

class InyeccionTest {
    @Test fun resuelveTodosLosViewModels() {
        val app = koinApplication { modules(appModule) }
        try {
            assertNotNull(app.koin.get<InicioViewModel>())
            assertNotNull(app.koin.get<CitasViewModel>())
            assertNotNull(app.koin.get<DetalleCitaViewModel>())
            assertNotNull(app.koin.get<SolicitudViewModel>())
            assertNotNull(app.koin.get<PerfilViewModel>())
        } finally {
            app.close()
        }
    }
}
