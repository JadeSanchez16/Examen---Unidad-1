package pe.upeu.andinasalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.core.context.GlobalContext
import pe.upeu.andinasalud.di.inicializarKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (GlobalContext.getOrNull() == null) inicializarKoin()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App { oscuro ->
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !oscuro
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
