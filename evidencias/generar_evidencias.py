"""Genera el expediente de evidencias con capturas reales y salidas Git."""

from __future__ import annotations

from datetime import datetime
from html import escape
from pathlib import Path
import subprocess

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    HRFlowable, Image, PageBreak, Paragraph, Preformatted,
    SimpleDocTemplate, Spacer, Table, TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
EVIDENCIAS = Path(__file__).resolve().parent
OUT = EVIDENCIAS / "AndinaSalud_evidencia_Android_Git.pdf"
FECHA = datetime.now().strftime("%d/%m/%Y")
INK = colors.HexColor("#18302D")
TEAL = colors.HexColor("#176B5D")
MUTED = colors.HexColor("#536560")
PALE = colors.HexColor("#EAF2EF")
LINE = colors.HexColor("#C9D9D3")
AMBER = colors.HexColor("#8A651E")
AMBER_PALE = colors.HexColor("#FFF4D9")


def git(*args: str) -> str:
    result = subprocess.run(
        ["git", "-c", f"safe.directory={ROOT.as_posix()}", *args],
        cwd=ROOT, check=True, capture_output=True, text=True, encoding="utf-8",
    )
    return result.stdout.rstrip()


def registrar_fuentes() -> None:
    fonts = Path("C:/Windows/Fonts")
    pdfmetrics.registerFont(TTFont("ArialDoc", str(fonts / "arial.ttf")))
    pdfmetrics.registerFont(TTFont("ArialDoc-Bold", str(fonts / "arialbd.ttf")))
    pdfmetrics.registerFontFamily("ArialDoc", normal="ArialDoc", bold="ArialDoc-Bold")
    pdfmetrics.registerFont(TTFont("ConsolasDoc", str(fonts / "consola.ttf")))


registrar_fuentes()
STYLES = {
    "kicker": ParagraphStyle("kicker", fontName="ArialDoc-Bold", fontSize=9, leading=13, textColor=TEAL, spaceAfter=10),
    "cover": ParagraphStyle("cover", fontName="ArialDoc-Bold", fontSize=30, leading=36, textColor=INK, spaceAfter=14),
    "h1": ParagraphStyle("h1", fontName="ArialDoc-Bold", fontSize=17, leading=22, textColor=INK, spaceAfter=12),
    "h2": ParagraphStyle("h2", fontName="ArialDoc-Bold", fontSize=11.5, leading=16, textColor=TEAL, spaceBefore=14, spaceAfter=7),
    "body": ParagraphStyle("body", fontName="ArialDoc", fontSize=9.5, leading=14.5, textColor=INK, spaceAfter=7),
    "small": ParagraphStyle("small", fontName="ArialDoc", fontSize=8.2, leading=12, textColor=INK),
    "muted": ParagraphStyle("muted", fontName="ArialDoc", fontSize=8.3, leading=12, textColor=MUTED, spaceAfter=6),
    "table_head": ParagraphStyle("table_head", fontName="ArialDoc-Bold", fontSize=8.2, leading=11, textColor=colors.white),
    "table": ParagraphStyle("table", fontName="ArialDoc", fontSize=8.1, leading=11.5, textColor=INK),
    "table_bold": ParagraphStyle("table_bold", fontName="ArialDoc-Bold", fontSize=8.1, leading=11.5, textColor=INK),
    "code": ParagraphStyle("code", fontName="ConsolasDoc", fontSize=7.1, leading=10.1, textColor=INK),
}


def p(text: str, style: str = "body") -> Paragraph:
    return Paragraph(text, STYLES[style])


def safe(text: str) -> str:
    return escape(text).replace("\n", "<br/>")


def title(text: str, kicker: str | None = None) -> list:
    items = []
    if kicker:
        items.append(p(kicker.upper(), "kicker"))
    items.extend([p(text, "h1"), HRFlowable(width="100%", thickness=1, color=LINE, spaceAfter=13)])
    return items


def section(text: str) -> Paragraph:
    return p(text, "h2")


def table(headers: list[str], rows: list[list[str]], widths: list[int]) -> Table:
    data = [[p(safe(x), "table_head") for x in headers]]
    for row in rows:
        data.append([p(safe(x), "table_bold" if index == 0 else "table") for index, x in enumerate(row)])
    t = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), TEAL),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7FAF9")]),
        ("LINEBELOW", (0, 0), (-1, 0), 0.8, TEAL),
        ("LINEBELOW", (0, 1), (-1, -1), 0.4, LINE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
    ]))
    return t


def notice(heading: str, body: str, tone: str = "neutral") -> Table:
    fill = AMBER_PALE if tone == "warning" else PALE
    accent = AMBER if tone == "warning" else TEAL
    content = [p(heading, "table_bold"), Spacer(1, 4), p(body, "small")]
    t = Table([[content]], colWidths=[503], hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), fill),
        ("LINEBEFORE", (0, 0), (0, 0), 3, accent),
        ("LEFTPADDING", (0, 0), (-1, -1), 12),
        ("RIGHTPADDING", (0, 0), (-1, -1), 12),
        ("TOPPADDING", (0, 0), (-1, -1), 10),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
    ]))
    return t


def page_frame(canvas, doc) -> None:
    width, height = A4
    canvas.saveState()
    if doc.page > 1:
        canvas.setFillColor(TEAL)
        canvas.rect(0, height - 12, width, 12, fill=1, stroke=0)
        canvas.setFont("ArialDoc-Bold", 8)
        canvas.setFillColor(TEAL)
        canvas.drawString(46, height - 36, "ANDINASALUD  /  EVIDENCIA DEL EXAMEN PARCIAL U1")
    canvas.setStrokeColor(LINE)
    canvas.line(46, 43, width - 46, 43)
    canvas.setFont("ArialDoc", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(46, 29, f"Jade Sanchez  |  {FECHA}  |  Android y Git")
    canvas.drawRightString(width - 46, 29, f"{doc.page:02d}")
    canvas.restoreState()


def capture(story: list, code: str, heading: str, image_file: str, requirement: str, observation: str, note: str) -> None:
    path = EVIDENCIAS / image_file
    if not path.is_file():
        raise FileNotFoundError(path)
    story.append(PageBreak())
    story.extend(title(heading, f"Evidencia visual {code}  |  Android"))
    iw, ih = ImageReader(str(path)).getSize()
    image = Image(str(path), width=265, height=265 * ih / iw)
    right = [
        p("REQUISITOS", "kicker"), p(requirement, "body"),
        section("Observación"), p(observation, "body"),
        section("Alcance de la captura"), p(note, "muted"),
        Spacer(1, 18), p(f"Archivo fuente: {safe(image_file)}", "muted"),
    ]
    block = Table([[image, right]], colWidths=[278, 225], hAlign="LEFT")
    block.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 0),
        ("RIGHTPADDING", (0, 0), (0, 0), 13),
        ("RIGHTPADDING", (1, 0), (1, 0), 0),
        ("TOPPADDING", (0, 0), (-1, -1), 0),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
    ]))
    story.append(block)


def main() -> None:
    head = git("rev-parse", "--short", "main")
    tag = git("rev-list", "-n", "1", "v1.0-unidad1")[:7]
    remote = git("remote", "get-url", "origin").removesuffix(".git")
    branches = git("branch", "-a")
    shortlog = git("shortlog", "-sne", "HEAD")
    graph = git("log", "--graph", "--oneline", "--all", "--simplify-by-decoration")
    ramas_activas = ["main", "develop", "feature/andinasalud-sanchez", "sc-a-sanchez", "sc-b-sanchez", "sc-c-sanchez", "sc-d-sanchez"]
    doc = SimpleDocTemplate(
        str(OUT), pagesize=A4, rightMargin=46, leftMargin=46,
        topMargin=62, bottomMargin=58, title="AndinaSalud - expediente de evidencias",
        author="Jade Sanchez", subject="Examen Parcial Unidad 1 - caso AndinaSalud",
    )
    story: list = []

    story.extend([Spacer(1, 70), p("EXAMEN PARCIAL  /  UNIDAD 1", "kicker"),
                  p("AndinaSalud<br/>Expediente de evidencias", "cover"),
                  HRFlowable(width="100%", thickness=3, color=TEAL, spaceAfter=25),
                  p("Producto Kotlin Multiplatform con datos simulados en memoria", "h1"),
                  p("Desarrollo individual: <b>Jade Sanchez</b><br/>Documento: 61098438<br/>Fecha del expediente: " + FECHA, "body"),
                  Spacer(1, 19),
                  notice("Resultado documentado", "Android: APK compilado e instalado en el emulador Pixel_9a; 13 pruebas automatizadas correctas y lint sin errores. Las capturas E01-E13 muestran la aplicación y flujos observables."),
                  Spacer(1, 11),
                  notice("Límites que no se sustituyen por texto", "No hay ejecución ni capturas iOS verificadas en Windows. El trabajo es de una sola autora: no se atribuyen aportes, revisiones ni defensa a un segundo integrante. El tag v1.0-unidad1 existe, pero no apunta al main más reciente.", "warning"),
                  Spacer(1, 24),
                  p("Documento de referencia: Examen Parcial U1 - Caso AndinaSalud, 13 páginas. Este expediente coteja las partes I y II, los requisitos técnicos, los entregables y la lista de cotejo; la Parte III requiere defensa personal ante el docente.", "muted")])

    story.append(PageBreak())
    story.extend(title("Alcance y criterio de evidencia", "01  /  Control documental"))
    story.append(p("Se usan tres fuentes distintas: código y pruebas del repositorio, capturas directas del emulador Android y salidas Git tomadas al generar este PDF. Una captura muestra un estado concreto; no demuestra por sí sola todos los casos límite.", "body"))
    story.append(table(["Fuente", "Dato comprobable", "Límite"], [
        ["Proyecto local", f"main {head}; módulos androidApp, iosApp y shared", "iOS configurado, no compilado en este host"],
        ["Android", "Emulador Pixel_9a, 1080 x 2424 px; capturas E01-E13", "Sesiones distintas; fechas semilla relativas al día de ejecución"],
        ["Pruebas", "13 pruebas en 4 suites; 0 fallos, 0 errores; lint 0 errores", "No equivalen a una prueba de simulador iOS"],
        ["Git", "Siete ramas locales y remotas; un autor real", "No acredita PR revisadas por otro integrante"],
    ], [105, 206, 192]))
    story.append(section("Repositorio y entrega"))
    story.append(p(f'<link href="{escape(remote)}" color="#176B5D">{safe(remote)}</link>', "body"))
    story.append(p("El README describe paquetes, decisiones de arquitectura, ejecución Android/iOS y los límites de verificación. La fuente de datos es en memoria: no hay API ni base de datos, y los cambios de citas se pierden al reiniciar el proceso.", "body"))
    story.append(section("Lectura de estados"))
    story.append(notice("Verificado / Configurado / Pendiente", "Verificado: observado en Android o respaldado por pruebas y código. Configurado: presente en el proyecto sin ejecución comprobada. Pendiente: exigencia del examen para la que no existe evidencia suficiente. No se equiparan estas categorías."))

    story.append(PageBreak())
    story.extend(title("Parte I - Requisitos funcionales", "02  /  RF-01 a RF-08"))
    story.append(table(["Código", "Exigencia y evidencia", "Ubicación principal"], [
        ["RF-01", "Inicio: saludo, próxima cita, Mis citas y Solicitar cita. E01; alta/cupo observados en emulador.", "presentation/inicio; ObtenerResumenCitasUseCase"],
        ["RF-02", "Citas ordenadas por cercanía y chips de estado. E02, E10; FiltroCitasTest.", "presentation/citas; FiltrarCitasUseCase"],
        ["RF-03", "Detalle con especialidad, médico, sede, fecha/hora, estado e indicaciones; cancelar con confirmación. E03 y E07.", "presentation/detalle; CancelarCitaUseCase"],
        ["RF-04", "Formulario con especialidad, sede, fecha, hora y motivo; errores bajo cada campo. E04 y E08.", "presentation/solicitud; ValidarCitaUseCase"],
        ["RF-05", "Búsqueda por especialidad o médico sin mayúsculas ni tildes. E09 y FiltroCitasTest.", "FiltrarCitasUseCase; CitasViewModel"],
        ["RF-06", "Perfil fijo y cambio inmediato de tema global claro/oscuro. E05, E12 y E13.", "presentation/perfil; presentation/theme; App.kt"],
        ["RF-07", "Barra inferior Inicio/Citas/Perfil, rutas a Detalle y Solicitud; retroceso del sistema observado.", "presentation/navigation/AppNavHost.kt; Destinos.kt"],
        ["RF-08", "Loading de 800 ms, contenido, vacío y error en pantallas que cargan datos. E10 muestra vacío; error y retardo revisados en ViewModels.", "ViewModels de inicio, citas, detalle, solicitud, perfil y reprogramación"],
    ], [49, 290, 164]))
    story.append(Spacer(1, 10))
    story.append(notice("Alcance de RF-08", "La evidencia visual incluye un estado vacío real (E10), pero no una captura de fallo inyectado. Los estados de error y el retardo se cotejaron con el código. No se presenta esa revisión de código como captura de ejecución."))

    story.append(PageBreak())
    story.extend(title("Parte I - Reglas y datos", "03  /  RN-01 a RN-05"))
    story.append(table(["Regla", "Implementación en dominio", "Comprobación"], [
        ["RN-01", "Rechaza fecha/hora no futura.", "ValidarCitaUseCase; ReglasDeNegocioTest"],
        ["RN-02", "Máximo tres Programadas del mismo paciente.", "ValidarCitaUseCase; SolicitarCitaUseCase; E01"],
        ["RN-03", "Solo cancela Programada si faltan más de 24 h.", "CancelarCitaUseCase; prueba de frontera; E07"],
        ["RN-04", "Motivo de 10 a 200 caracteres.", "ValidarCitaUseCase; límites 9/10/200/201; E08"],
        ["RN-05", "Sin duplicado Programada del paciente en día y hora/minuto.", "ValidarCitaUseCase; ReglasDeNegocioTest"],
    ], [55, 234, 214]))
    story.append(section("Fuente simulada mínima"))
    story.append(table(["Conjunto", "Contenido presente"], [
        ["Paciente", "Jade Sanchez; documento 61098438; correo jade.sanchez@gmail.com; teléfono 997 652 798."],
        ["Sedes", "Ñaña, Chosica, Chaclacayo y Santa Anita."],
        ["Especialidades", "Medicina General, Odontología, Pediatría, Nutrición y Psicología."],
        ["Médicos", "Diez: dos por especialidad, cada uno con sedes asignadas."],
        ["Citas", "Seis al iniciar: 3 Programadas futuras, 2 Atendidas y 1 Cancelada. Fechas relativas al reloj local."],
    ], [88, 415]))
    story.append(Spacer(1, 9))
    story.append(p("Fuente verificable: data/local/CitasSimuladas.kt y DatosSimuladosTest.kt. Las fechas del anexo del examen son sugeridas, no valores que deban permanecer fijos; el proyecto las desplaza para conservar tres citas futuras.", "muted"))

    story.append(PageBreak())
    story.extend(title("Parte I - Arquitectura y plataforma", "04  /  Requisitos técnicos"))
    story.append(table(["Ámbito", "Implementación observada", "Estado"], [
        ["KMP", "shared/commonMain; targets Android, iosArm64 e iosSimulatorArm64; entradas androidApp e iosApp.", "Android verificado; iOS configurado"],
        ["Dominio", "Entidades data class, EstadoCita sealed class, null-safety y reglas en usecase.", "Cotejado en código"],
        ["Compose", "Composables compartidos, estado elevado y LazyColumn para citas.", "Android verificado"],
        ["Navegación / tema", "Scaffold, tres destinos, rutas constantes, Material 3 y paletas propias clara/oscura.", "Android verificado"],
        ["Clean + MVVM", "domain/repository, data/repository, casos de uso, ViewModels, StateFlow y UiState.", "Cotejado en código"],
        ["Koin", "AppModule en commonMain e inicialización por plataforma.", "Android verificado; iOS no ejecutado"],
        ["Asincronía", "Corrutinas en ViewModels y delay(800) para carga simulada.", "Cotejado en código"],
        ["Responsividad", "Pantallas observadas en teléfono vertical 1080 x 2424; chips con FlowRow.", "Android verificado en ese tamaño"],
        ["Restricción", "Sin Ktor, Retrofit, Room, SQLDelight, red ni persistencia en la app.", "Cotejado en dependencias"],
    ], [100, 302, 101]))
    story.append(section("Sustitución futura de datos"))
    story.append(p("La interfaz CitaRepository vive en domain/repository; CitaRepositoryFake la implementa en data/repository y se enlaza en di/AppModule.kt. Con una API futura se sustituiría la implementación y su binding, conservando la UI y los casos de uso. Esa API no forma parte de esta entrega.", "body"))

    story.append(PageBreak())
    story.extend(title("Parte II - Solicitudes de cambio", "05  /  SC-A a SC-D"))
    story.append(table(["SC", "Comportamiento implementado", "Rastro"], [
        ["SC-A", "Chip Hoy combinado con estado; filtro resuelto en caso de uso/ViewModel, no en composable.", "E02, E10; FiltrarCitasUseCase; FiltroCitasTest"],
        ["SC-B", "Indicador de Programadas en barra inferior; Solicitar cita se bloquea al llegar a tres mediante resumen de dominio.", "E01, E02; ObtenerResumenCitasUseCase; ValidarCitaUseCase"],
        ["SC-C", "Modalidad Presencial/Teleconsulta en dominio, formulario, lista y detalle; iconos distintos.", "E02, E03, E04; ModalidadAtencion.kt; prueba de solicitud"],
        ["SC-D", "Reprograma Programada con fecha/hora futuras, reutiliza validación y conserva historial en detalle.", "E06, E11; ReprogramarCitaUseCase; ReglasDeNegocioTest"],
    ], [49, 280, 174]))
    story.append(Spacer(1, 13))
    story.append(notice("Asignación individual", "El examen asigna una solicitud distinta a cada estudiante. A petición de la autora, este proyecto individual integra las cuatro solicitudes. Sus ramas sc-a-sanchez, sc-b-sanchez, sc-c-sanchez y sc-d-sanchez contienen historial propio; no se atribuye trabajo a otra persona."))
    story.append(section("Defensa técnica (Parte III)"))
    story.append(p("La defensa no es un artefacto que pueda declararse aprobado en este PDF. Para sustentarla se debe mostrar en vivo el código de las reglas, el recorrido repositorio - caso de uso - ViewModel - composable, StateFlow/UiState, el tema, Koin y el historial Git. Este expediente únicamente indica dónde consultar esas piezas.", "body"))

    story.append(PageBreak())
    story.extend(title("Validación reproducible", "06  /  Compilación, pruebas y recorrido"))
    story.append(table(["Verificación", "Resultado documentado"], [
        ["Compilación", ":shared:testAndroidHostTest y :androidApp:assembleDebug - BUILD SUCCESSFUL."],
        ["Pruebas", "13 pruebas: 7 ReglasDeNegocioTest, 3 FiltroCitasTest, 2 DatosSimuladosTest, 1 InyeccionTest. Sin fallos ni errores."],
        ["Lint", ":androidApp:lintDebug - 0 errores; avisos no bloqueantes de versiones/recursos."],
        ["Emulador", "APK instalado en emulator-5554 (Pixel_9a). Se recorrieron Inicio, Citas, Perfil, Detalle, Solicitud, filtros, búsqueda, cancelación, tema y reprogramación."],
        ["Sesión de prueba", "Se canceló una cita simulada, se comprobó el cupo, se validó formulario vacío y se reprogramó otra cita; E07-E11 conservan esos estados."],
        ["iOS", "Targets y entrada de aplicación presentes. Compilación, simulador y capturas no verificados en Windows."],
    ], [110, 393]))
    story.append(section("Comandos para repetir la verificación"))
    story.append(Preformatted(".\\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :androidApp:lintDebug\nadb install -r androidApp\\build\\outputs\\apk\\debug\\androidApp-debug.apk", STYLES["code"]))
    story.append(Spacer(1, 12))
    story.append(notice("Alcance temporal", "Las capturas E01-E06, E12 y E13 provienen de una sesión Android anterior; E07-E11, de una sesión posterior. La fuente semilla calcula fechas relativas al momento de ejecución. La diferencia de fechas y contadores entre capturas es esperada y no implica datos persistidos."))

    story.append(PageBreak())
    story.extend(title("Condiciones y rúbrica del examen", "07  /  Marco de evaluación"))
    story.append(table(["Bloque", "Tiempo", "Objeto de evaluación"], [
        ["1", "15 min", "Indicaciones y verificación del repositorio y del entorno."],
        ["2 - Parte I", "45 min", "Producto funcionando y recorrido de RF-01 a RF-08 en Android e iOS."],
        ["3 - Parte II", "120 min", "Solicitud de cambio individual en su rama, con al menos tres commits distribuidos."],
        ["4 - Parte III", "45 min", "Defensa técnica individual, aproximadamente 15 min por estudiante."],
        ["5", "15 min", "Subida de ramas, tag del commit evaluado y evidencias."],
    ], [100, 65, 338]))
    story.append(section("Ponderación de los ocho criterios (20 puntos)"))
    story.append(table(["Criterio", "Pts.", "Evidencia en este expediente"], [
        ["Entorno multiplataforma", "2", "Android sí; iOS pendiente"],
        ["Dominio commonMain", "3", "Modelo, RN y pruebas"],
        ["Interfaz Compose", "3", "RF-01 a RF-08; E01-E13"],
        ["Navegación y Material 3", "2", "E01-E06, E12-E13"],
        ["Clean + MVVM", "2", "Matriz técnica y código"],
        ["Colaboración Git", "2", "Una autora; criterio de pareja no acreditado"],
        ["Solicitud de cambio", "4", "SC-A a SC-D; cuatro ramas"],
        ["Defensa técnica", "2", "Pendiente de evaluación presencial"],
    ], [194, 42, 267]))
    story.append(Spacer(1, 9))
    story.append(p("El examen indica nota mínima aprobatoria 13 y que, sin demostrar ambas plataformas, la calificación no puede superar 14. El uso de IA exige poder explicar y justificar el código durante la defensa. Este documento no acredita horarios, asistencia ni respuestas ante el docente.", "muted"))

    story.append(PageBreak())
    story.extend(title("Guía para la defensa técnica", "08  /  Banco de 11 preguntas"))
    story.append(p("El docente elige dos preguntas por estudiante. Estas referencias permiten localizar el código propio; no sustituyen una explicación en vivo.", "body"))
    story.append(table(["N.º", "Tema de la pregunta oficial", "Dónde mostrarlo"], [
        ["1", "RN-02 y ubicación en dominio", "ValidarCitaUseCase.kt; SolicitarCitaUseCase.kt"],
        ["2", "Sustitución por API futura", "CitaRepository.kt; CitaRepositoryFake.kt; AppModule.kt"],
        ["3", "Estado sealed frente a enum/texto", "EstadoCita.kt"],
        ["4", "Dato desde fuente hasta pantalla", "CitasSimuladas.kt - repositorio - usecase - ViewModel - Screen"],
        ["5", "UiState frente a modelo de dominio", "CitasUiState.kt; Cita.kt"],
        ["6", "Corrutina, retardo y destrucción", "viewModelScope y delay(800) en ViewModels"],
        ["7", "StateFlow de solo lectura", "CitasViewModel.kt y otros ViewModels"],
        ["8", "Composable reutilizable", "CitaComponents.kt; CitaItem"],
        ["9", "Cambio de tema global", "PerfilScreen.kt; App.kt; AndinaSaludTheme.kt"],
        ["10", "Agregar otra especialidad", "CitasSimuladas.kt: catálogo y médicos asignados"],
        ["11", "Rama, aporte y conflictos reales", "Gráfico Git, ramas SC y commits; relato personal"],
    ], [32, 191, 280]))
    story.append(Spacer(1, 10))
    story.append(notice("No inventar una defensa", "La pregunta 11 requiere describir aportes y conflictos efectivamente vividos. El expediente confirma una sola autora y no fabrica conflictos, revisores ni respuestas que no se hayan dado ante el docente.", "warning"))

    captures = [
        ("E01", "Inicio y cupo", "andina-inicio.png", "RF-01, RF-07, SC-B, RN-02", "Saludo, próxima cita, accesos rápidos, indicador 3 y solicitud deshabilitada al alcanzar el límite.", "Estado inicial de una sesión. La captura no demuestra por sí sola la regla de dominio."),
        ("E02", "Listado de citas", "andina-citas.png", "RF-02, RF-05, SC-A, SC-B, SC-C", "Búsqueda, filtros de estado, chip Hoy, badge y modalidades visibles en la lista.", "La ordenación y combinación de filtros se verifican además en FiltroCitasTest."),
        ("E03", "Detalle de cita", "andina-detalle.png", "RF-03, SC-C, SC-D", "Especialidad, médico, sede, fecha/hora, estado, indicaciones, modalidad y acciones.", "La confirmación de cancelación se documenta aparte en E07."),
        ("E04", "Solicitud de cita", "andina-solicitud.png", "RF-04, SC-C", "Campos obligatorios y selector de modalidad antes de enviar.", "La validación por campo se muestra en E08; las reglas de dominio se prueban en la suite."),
        ("E05", "Perfil en modo claro", "andina-perfil.png", "RF-06", "Datos fijos de Jade Sanchez y conmutador entre Modo claro y Modo oscuro.", "E12 y E13 muestran el alcance del tema fuera de Perfil."),
        ("E06", "Formulario de reprogramación", "andina-reprogramacion.png", "SC-D", "Nueva fecha y hora para una cita Programada.", "E11 muestra la actualización y su registro de cambios tras confirmar."),
        ("E07", "Confirmación de cancelación", "andina-confirmacion.png", "RF-03, RN-03", "Diálogo con Volver y Confirmar antes de modificar la cita.", "La condición de más de 24 h está en CancelarCitaUseCase y su prueba de frontera."),
        ("E08", "Errores por campo", "andina-validacion.png", "RF-04, RN-01, RN-04", "Errores bajo especialidad, sede, fecha, hora y motivo al enviar vacío.", "Los límites temporales y de longitud se prueban también en ReglasDeNegocioTest."),
        ("E09", "Búsqueda sin tilde", "andina-busqueda.png", "RF-05", "El texto odontologia devuelve Odontología en la lista.", "El filtrado por médico y mayúsculas se verifica en FiltroCitasTest."),
        ("E10", "Filtro Hoy y estado vacío", "andina-filtro-hoy.png", "RF-08, SC-A", "Hoy combinado con Programada produce el mensaje de lista vacía.", "La semilla no tiene cita en el día de esa captura."),
        ("E11", "Historial de reprogramación", "andina-historial.png", "SC-D", "Detalle actualizado y registro de horario anterior y nuevo.", "La operación se hizo solo sobre datos de memoria del emulador."),
        ("E12", "Perfil en modo oscuro", "andina-perfil-oscuro-final.png", "RF-06", "El selector activo y la paleta oscura se observan en Perfil.", "La captura siguiente comprueba que el cambio alcanza otra pantalla."),
        ("E13", "Citas en modo oscuro", "andina-citas-oscuro.png", "RF-06, RF-07", "Lista, chips y barra inferior adoptan la paleta oscura.", "Es una sesión distinta de E09-E11; los contadores pueden variar."),
    ]
    for args in captures:
        capture(story, *args)

    story.append(PageBreak())
    story.extend(title("Historial Git verificable", "09  /  Corte del repositorio"))
    story.append(p(f"Salida obtenida al generar este expediente. Main: <b>{head}</b>. Tag v1.0-unidad1: <b>{tag}</b>. El tag corresponde a un commit anterior al main actual; no se presenta como si etiquetara esta revisión.", "body"))
    story.append(section("Gráfico de puntas - git log --graph --oneline --all --simplify-by-decoration"))
    story.append(Preformatted(graph, STYLES["code"]))
    story.append(section("Ramas activas y commit de punta"))
    story.append(table(["Rama", "SHA"], [[rama, git("rev-parse", "--short", rama)] for rama in ramas_activas], [344, 159]))
    story.append(section("Autores - git shortlog -sne HEAD"))
    story.append(Preformatted(shortlog, STYLES["code"]))

    story.append(PageBreak())
    story.extend(title("Ramas y trazabilidad", "10  /  Git y proceso"))
    story.append(section("git branch -a"))
    story.append(Preformatted(branches, STYLES["code"]))
    story.append(section("Integración hacia main - git log --first-parent main --oneline -n 9"))
    story.append(Preformatted(git("log", "--first-parent", "main", "--oneline", "-n", "9"), STYLES["code"]))
    story.append(Spacer(1, 15))
    story.append(notice("Trabajo individual, sin autoría simulada", "El shortlog registra una sola autora. No se adjuntan enlaces verificables de solicitudes de incorporación con revisión de un compañero; tampoco se afirma que existan. El examen exige dos integrantes y revisión cruzada: esta adaptación individual no acredita ese criterio.", "warning"))
    story.append(Spacer(1, 10))
    story.append(p("Las ramas activas son main, develop, feature/andinasalud-sanchez y sc-a/b/c/d-sanchez, conforme a la estructura solicitada por la autora. No hay una rama fix/* activa. Los commits de las SC conservan prefijos descriptivos y superan el mínimo de tres por rama, según el historial local.", "body"))
    story.append(section("Convención de mensajes documentada"))
    story.append(p("feat: funcionalidad; fix: corrección; refactor: reorganización; style: formato o tema; docs: documentación. El historial adjunto permite revisar los mensajes reales, no solo la convención declarada.", "small"))

    story.append(PageBreak())
    story.extend(title("Entregables y lista de cotejo", "11  /  Estado sin sustituciones"))
    story.append(table(["Exigencia del examen", "Evidencia en esta entrega", "Estado"], [
        ["Repositorio y tag v1.0-unidad1", f"Repositorio enlazado; tag {tag}; main {head}.", "Parcial: tag anterior"],
        ["Seis pantallas en Android e iOS", "Seis pantallas Android: E01-E06. No hay capturas iOS.", "Parcial"],
        ["README de paquetes, decisiones y ejecución", "README.md del proyecto.", "Disponible"],
        ["Rama individual SC", "sc-a-sanchez a sc-d-sanchez.", "Disponible"],
        ["Gráfico Git y shortlog", "Salidas reproducibles en este PDF; no captura de terminal separada.", "Contenido disponible"],
        ["PR cerradas y revisión cruzada", "No se adjuntan enlaces ni comentarios verificables de otro integrante.", "No acreditado"],
    ], [149, 259, 95]))
    story.append(section("Cotejo previo, puntos 1-8"))
    story.append(table(["N.º", "Criterio", "Situación"], [
        ["1", "Ejecución Android e iOS", "Android sí; iOS pendiente"],
        ["2", "Seis pantallas alcanzables", "Android sí"],
        ["3", "Estado sealed class", "Sí, EstadoCita"],
        ["4", "Cinco RN en dominio", "Sí, casos de uso"],
        ["5", "ViewModels con StateFlow", "Sí"],
        ["6", "Interfaz repositorio domain / fake data", "Sí"],
        ["7", "Carga, vacío y error en pantallas con datos", "Código cotejado; E10 vacío"],
        ["8", "Material 3, paleta y ambos temas", "Android E05, E12, E13"],
    ], [33, 266, 204]))

    story.append(PageBreak())
    story.extend(title("Cotejo final y pendientes", "12  /  Puntos 9-15"))
    story.append(table(["N.º", "Criterio", "Situación"], [
        ["9", "Sin dependencias de red o BD", "Sí, revisado en Gradle"],
        ["10", "Ramas/commits de cada integrante", "Un solo integrante; no acredita pareja"],
        ["11", "Funcionalidad integrada a main desde develop", "Historial first-parent adjunto"],
        ["12", "Dos PR revisadas por el compañero", "No acreditado"],
        ["13", "Mensajes convencionales y tag evaluado", f"Convención en Git; tag {tag} anterior a {head}"],
        ["14", "README y reparto del equipo", "README actualizado; desarrollo individual"],
        ["15", "Tres commits propios durante el examen", "SC con más de tres; horario de examen no certificable"],
    ], [33, 266, 204]))
    story.append(section("Qué falta para acreditar literalmente el examen"))
    story.append(notice("Pendientes verificables", "1. Ejecutar y capturar las seis pantallas en iOS con macOS/Xcode. 2. Si el commit evaluado debe ser el main actual, resolver explícitamente la discrepancia del tag sin reescribirlo a escondidas. 3. Aportar enlaces reales de PR y revisiones si existieran; una sola persona no puede generar revisión cruzada auténtica. 4. Realizar la defensa técnica ante el docente.", "warning"))
    story.append(Spacer(1, 13))
    story.append(p("Este PDF documenta lo realizado y lo comprobable. No reemplaza la demostración en vivo, no inventa capturas iOS ni declara cumplido un criterio de colaboración de dos personas que no ocurrió.", "muted"))

    doc.build(story, onFirstPage=page_frame, onLaterPages=page_frame)
    print(OUT)


if __name__ == "__main__":
    main()
