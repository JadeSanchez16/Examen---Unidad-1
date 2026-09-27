package pe.upeu.andinasalud

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.GlobalContext
import pe.upeu.andinasalud.di.inicializarKoin

fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) inicializarKoin()
    App()
}
