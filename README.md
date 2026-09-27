# AndinaSalud

Aplicacion de citas medicas Kotlin Multiplatform para Android e iOS. Esta entrega usa exclusivamente datos simulados en memoria. No incluye servicios web, base de datos ni persistencia; los cambios desaparecen al reiniciar el proceso.

## Estructura

- `androidApp`: punto de entrada Android.
- `iosApp`: aplicacion Xcode y punto de entrada Swift para el framework compartido.
- `shared/src/commonMain/kotlin/pe/upeu/andinasalud/domain`: entidades, contrato `CitaRepository`, reglas y casos de uso.
- `shared/src/commonMain/kotlin/pe/upeu/andinasalud/data`: fuente en memoria e implementacion `CitaRepositoryFake`.
- `shared/src/commonMain/kotlin/pe/upeu/andinasalud/presentation`: ViewModels, estados de interfaz, Compose, navegacion y tema.
- `shared/src/commonMain/kotlin/pe/upeu/andinasalud/di`: modulo Koin.

La UI consume `StateFlow` de ViewModels; estos llaman casos de uso. Los casos de uso solo conocen la interfaz del repositorio, que esta en dominio. La implementacion fake esta en data. Para conectar una API futura habria que crear otra implementacion de ese contrato y cambiar el binding de Koin, manteniendo la UI y los casos de uso.

Las reglas RN-01 a RN-05 estan en `ValidarCitaUseCase`, `SolicitarCitaUseCase` y `CancelarCitaUseCase`. La lista usa `LazyColumn`, busqueda sin tildes ni distincion de mayusculas, y filtros de estado. El tema Material 3 tiene paletas clara y oscura. La navegacion incluye Inicio, Citas, Perfil, Ajustes, Solicitud y Detalle. El estado de carga simulado dura 800 ms en las pantallas que consultan citas o paciente.

## Ejecutar

- Android: abrir el proyecto en Android Studio y ejecutar `androidApp`, o usar `./gradlew :androidApp:assembleDebug` (`.\gradlew.bat :androidApp:assembleDebug` en Windows). Instalar el APK en un emulador o dispositivo Android.
- iOS: abrir `iosApp/iosApp.xcodeproj` en Xcode sobre macOS, seleccionar un simulador o dispositivo iOS y ejecutar. Los targets `iosArm64` e `iosSimulatorArm64` estan configurados en `shared`.
- Pruebas de reglas: `./gradlew :shared:testAndroidHostTest` en un host con Android SDK.

El proyecto se desarrolla individualmente por Jade Sanchez. Los requisitos de revision entre companeros del documento de examen no pueden acreditarse de forma honesta en esta adaptacion individual.
