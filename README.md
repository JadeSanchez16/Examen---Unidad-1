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

Las reglas RN-01 a RN-05 estan en `ValidarCitaUseCase`, `SolicitarCitaUseCase` y `CancelarCitaUseCase`. La lista usa `LazyColumn`, busqueda sin tildes ni distincion de mayusculas, y filtros de estado. El tema Material 3 tiene paletas clara y oscura, con el selector directamente en Perfil. La navegacion incluye Inicio, Citas, Perfil, Solicitud, Detalle y Reprogramacion. Las pantallas que consultan citas, paciente u opciones muestran carga simulada de 800 ms, contenido, estado vacio y error.

Las cuatro solicitudes de cambio estan integradas: SC-A agrega el filtro Hoy combinable; SC-B muestra el numero de citas programadas y usa el cupo de dominio para habilitar solicitudes; SC-C incorpora modalidad Presencial/Teleconsulta en el modelo, formulario, lista y detalle; SC-D permite reprogramar una cita Programada y registra el cambio en su detalle. Cada solicitud se desarrollo en una rama `sc-<letra>-sanchez` con seis commits propios.

## Ejecutar

- Android: abrir el proyecto en Android Studio y ejecutar `androidApp`, o usar `./gradlew :androidApp:assembleDebug` (`.\gradlew.bat :androidApp:assembleDebug` en Windows). Instalar el APK en un emulador o dispositivo Android.
- iOS: abrir `iosApp/iosApp.xcodeproj` en Xcode sobre macOS, seleccionar un simulador o dispositivo iOS y ejecutar. Los targets `iosArm64` e `iosSimulatorArm64` estan configurados en `shared`.
- Pruebas de reglas e inyeccion: `./gradlew :shared:testAndroidHostTest` en un host con Android SDK.

El proyecto se desarrolla individualmente por Jade Sanchez. Las ramas `feature/*`, `fix/*` y `sc-*-sanchez` registran su trabajo, con integracion mediante `develop` antes de `main`. Los requisitos de dos autores y revision cruzada del examen no pueden acreditarse de forma honesta en esta adaptacion individual. La compilacion y ejecucion iOS requieren macOS y Xcode; no se han verificado en Windows.
