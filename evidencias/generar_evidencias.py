"""Genera el expediente de evidencias con capturas reales y salidas Git."""

from __future__ import annotations

from html import escape
from io import BytesIO
from pathlib import Path
import subprocess

from PIL import Image as PILImage, ImageDraw
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
INK = colors.HexColor("#18302D")
TEAL = colors.HexColor("#176B5D")
MUTED = colors.HexColor("#536560")
PALE = colors.HexColor("#EAF2EF")
LINE = colors.HexColor("#C9D9D3")


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
    "institution": ParagraphStyle("institution", fontName="ArialDoc-Bold", fontSize=12, leading=17, textColor=INK, alignment=1, spaceAfter=7),
    "institution_sub": ParagraphStyle("institution_sub", fontName="ArialDoc", fontSize=9.5, leading=14, textColor=MUTED, alignment=1, spaceAfter=4),
    "cover_label": ParagraphStyle("cover_label", fontName="ArialDoc-Bold", fontSize=10, leading=15, textColor=TEAL, alignment=1, spaceAfter=12),
    "cover_center": ParagraphStyle("cover_center", fontName="ArialDoc-Bold", fontSize=25, leading=31, textColor=INK, alignment=1, spaceAfter=11),
    "cover_sub": ParagraphStyle("cover_sub", fontName="ArialDoc-Bold", fontSize=14, leading=19, textColor=TEAL, alignment=1, spaceAfter=16),
    "kicker": ParagraphStyle("kicker", fontName="ArialDoc-Bold", fontSize=9, leading=13, textColor=TEAL, spaceAfter=10),
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


def notice(heading: str, body: str) -> Table:
    content = [p(heading, "table_bold"), Spacer(1, 4), p(body, "small")]
    t = Table([[content]], colWidths=[503], hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), PALE),
        ("LINEBEFORE", (0, 0), (0, 0), 3, TEAL),
        ("LEFTPADDING", (0, 0), (-1, -1), 12),
        ("RIGHTPADDING", (0, 0), (-1, -1), 12),
        ("TOPPADDING", (0, 0), (-1, -1), 10),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
    ]))
    return t


def page_frame(canvas, doc) -> None:
    width, height = A4
    canvas.saveState()
    canvas.setFillColor(TEAL)
    canvas.rect(0, height - 12, width, 12, fill=1, stroke=0)
    if doc.page > 1:
        canvas.setFont("ArialDoc-Bold", 8)
        canvas.setFillColor(TEAL)
        canvas.drawString(46, height - 36, "ANDINASALUD  /  EVIDENCIA DE LAS PARTES I Y II")
    canvas.setStrokeColor(LINE)
    canvas.line(46, 43, width - 46, 43)
    canvas.setFont("ArialDoc", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(46, 29, "Jade Sanchez  |  AndinaSalud  |  Partes I y II")
    canvas.drawRightString(width - 46, 29, f"{doc.page:02d}")
    canvas.restoreState()


def capture(story: list, code: str, heading: str, image_file: str, requirement: str, observation: str, note: str) -> None:
    path = EVIDENCIAS / image_file
    if not path.is_file():
        raise FileNotFoundError(path)
    story.append(PageBreak())
    story.extend(title(heading, f"Evidencia visual {code}  |  Android"))
    iw, ih = ImageReader(str(path)).getSize()
    if image_file in {"andina-perfil.png", "andina-perfil-oscuro-final.png"}:
        with PILImage.open(path) as original:
            redacted = original.convert("RGB")
            background = redacted.getpixel((900, 550))
            ImageDraw.Draw(redacted).rectangle((0, 500, iw, 640), fill=background)
            buffer = BytesIO()
            redacted.save(buffer, format="PNG")
            buffer.seek(0)
        image = Image(buffer, width=265, height=265 * ih / iw)
        note += " El campo de identificación se oculta únicamente en esta copia del PDF; la aplicación y la captura fuente no cambian."
    else:
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
        topMargin=62, bottomMargin=58, title="AndinaSalud - informe de evidencias Partes I y II",
        author="Jade Sanchez", subject="Examen Parcial Unidad 1 - Partes I y II",
    )
    story: list = []

    story.extend([
        Spacer(1, 58),
        p("UNIVERSIDAD PERUANA UNIÓN", "institution"),
        p("FACULTAD DE INGENIERÍA Y ARQUITECTURA", "institution_sub"),
        p("ESCUELA PROFESIONAL DE INGENIERÍA DE SISTEMAS", "institution_sub"),
        Spacer(1, 67),
        p("DESARROLLO DE APLICACIONES MÓVILES", "cover_label"),
        p("INFORME TÉCNICO DE EVIDENCIAS", "cover_center"),
        p("EXAMEN PARCIAL - UNIDAD 1", "cover_sub"),
        HRFlowable(width="68%", thickness=2, color=TEAL, spaceAfter=25, hAlign="CENTER"),
        p("Caso AndinaSalud", "institution"),
        p("Parte I: producto del caso<br/>Parte II: solicitudes de cambio", "institution_sub"),
        Spacer(1, 51),
        p("Estudiante: <b>Jade Sanchez</b><br/>Modalidad de desarrollo: individual", "body"),
        Spacer(1, 14),
        notice("Objeto de estudio", "Aplicación Kotlin Multiplatform de gestión de citas médicas, implementada con datos simulados en memoria. Este informe documenta el código fuente, la ejecución Android, las pruebas y la trazabilidad Git correspondientes a las Partes I y II."),
    ])

    story.append(PageBreak())
    story.extend(title("Propósito y método de verificación", "01  /  Presentación del expediente"))
    story.append(p("El objetivo es relacionar los requisitos del caso AndinaSalud con las decisiones de implementación y con evidencias reproducibles. El análisis se organiza conforme al documento oficial: Parte I (producto y arquitectura) y Parte II (solicitudes de cambio). La adaptación individual corresponde a la autoría real del repositorio.", "body"))
    story.append(section("Fuentes de evidencia"))
    story.append(table(["Fuente", "Procedimiento", "Resultado incorporado"], [
        ["Código fuente", "Inspección de los módulos androidApp, iosApp y shared.", "Ubicación de entidades, reglas, DI, estado y pantallas."],
        ["Ejecución Android", "Instalación del APK en Pixel_9a (1080 x 2424 px) y recorrido de flujos.", "Capturas E01-E13 y observaciones asociadas."],
        ["Pruebas y Lint", "Ejecución de tareas Gradle para pruebas de host, APK y análisis estático.", "13 pruebas aprobadas, sin fallos; Lint sin errores."],
        ["Repositorio Git", "Consulta de ramas, grafo, autores y etiqueta del commit evaluado.", "Transcripciones reproducibles en el apartado Git."],
    ], [103, 214, 186]))
    story.append(section("Repositorio del producto"))
    story.append(p(f'<link href="{escape(remote)}" color="#176B5D">{safe(remote)}</link>', "body"))
    story.append(p("El archivo README.md describe la estructura de paquetes, la separación arquitectónica y las instrucciones de ejecución. La aplicación utiliza exclusivamente datos simulados en memoria, sin servicios web ni base de datos; por ello, las modificaciones de citas se reinician con el proceso.", "body"))
    story.append(notice("Criterio de lectura", "Cada captura documenta un estado observado en Android. Las pruebas verifican reglas y casos límite, mientras que las transcripciones Git identifican la autoría y las referencias del repositorio. Ninguna de estas fuentes sustituye a las otras."))

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
    story.append(notice("Lectura de RF-08", "E10 documenta visualmente el estado vacío. Los estados de carga, contenido y error, así como delay(800), se identifican en los ViewModels y en las pantallas correspondientes; no se atribuye a E10 la demostración de un fallo inyectado."))

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
        ["Paciente", "Jade Sanchez; nombre, documento, correo y teléfono presentes en la fuente simulada. El identificador se reserva fuera de este informe."],
        ["Sedes", "Ñaña, Chosica, Chaclacayo y Santa Anita."],
        ["Especialidades", "Medicina General, Odontología, Pediatría, Nutrición y Psicología."],
        ["Médicos", "Diez: dos por especialidad, cada uno con sedes asignadas."],
        ["Citas", "Seis al iniciar: 3 Programadas futuras, 2 Atendidas y 1 Cancelada. Fechas relativas al reloj local."],
    ], [88, 415]))
    story.append(Spacer(1, 9))
    story.append(p("Fuente verificable: data/local/CitasSimuladas.kt y DatosSimuladosTest.kt. Las fechas del anexo del examen son sugeridas, no valores que deban permanecer fijos; el proyecto las desplaza para conservar tres citas futuras.", "muted"))

    story.append(PageBreak())
    story.extend(title("Parte I - Arquitectura y plataforma", "04  /  Requisitos técnicos"))
    story.append(table(["Ámbito", "Implementación documentada", "Fuente"], [
        ["KMP", "shared/commonMain; targets Android, iosArm64 e iosSimulatorArm64; entradas androidApp e iosApp.", "Gradle y entradas"],
        ["Dominio", "Entidades data class, EstadoCita sealed class, null-safety y reglas en usecase.", "domain/"],
        ["Compose", "Composables compartidos, estado elevado y LazyColumn para citas.", "presentation/"],
        ["Navegación / tema", "Scaffold, tres destinos, rutas constantes, Material 3 y paletas propias clara/oscura.", "AppNavHost; theme/"],
        ["Clean + MVVM", "domain/repository, data/repository, casos de uso, ViewModels, StateFlow y UiState.", "shared/commonMain"],
        ["Koin", "AppModule en commonMain e inicialización en los puntos de entrada de plataforma.", "di/; entradas"],
        ["Asincronía", "Corrutinas en ViewModels y delay(800) para carga simulada.", "ViewModels"],
        ["Responsividad", "Pantallas observadas en teléfono vertical 1080 x 2424; chips con FlowRow.", "E01-E13"],
        ["Restricción", "Sin Ktor, Retrofit, Room, SQLDelight, red ni persistencia en la app.", "Gradle; data/"],
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

    story.append(PageBreak())
    story.extend(title("Validación reproducible", "06  /  Compilación, pruebas y recorrido"))
    story.append(table(["Verificación", "Resultado documentado"], [
        ["Compilación", ":shared:testAndroidHostTest y :androidApp:assembleDebug - BUILD SUCCESSFUL."],
        ["Pruebas", "13 pruebas: 7 ReglasDeNegocioTest, 3 FiltroCitasTest, 2 DatosSimuladosTest, 1 InyeccionTest. Sin fallos ni errores."],
        ["Lint", ":androidApp:lintDebug - 0 errores; avisos no bloqueantes de versiones/recursos."],
        ["Emulador", "APK instalado en emulator-5554 (Pixel_9a). Se recorrieron Inicio, Citas, Perfil, Detalle, Solicitud, filtros, búsqueda, cancelación, tema y reprogramación."],
        ["Sesión de prueba", "Se canceló una cita simulada, se comprobó el cupo, se validó formulario vacío y se reprogramó otra cita; E07-E11 conservan esos estados."],
    ], [110, 393]))
    story.append(section("Comandos para repetir la verificación"))
    story.append(Preformatted(".\\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :androidApp:lintDebug\nadb install -r androidApp\\build\\outputs\\apk\\debug\\androidApp-debug.apk", STYLES["code"]))
    story.append(Spacer(1, 12))
    story.append(notice("Alcance temporal", "Las capturas E01-E06, E12 y E13 provienen de una sesión Android anterior; E07-E11, de una sesión posterior. La fuente semilla calcula fechas relativas al momento de ejecución. La diferencia de fechas y contadores entre capturas es esperada y no implica datos persistidos."))

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
    story.extend(title("Historial Git y autoría", "07  /  Evidencia reproducible"))
    story.append(p("Las salidas de este apartado se obtienen del repositorio al generar el documento. Constituyen un corte verificable del historial; los identificadores de punta pueden avanzar al integrar una revisión posterior del propio informe.", "body"))
    story.append(section("Grafo de referencias - git log --graph --oneline --all --simplify-by-decoration"))
    story.append(Preformatted(graph, STYLES["code"]))
    story.append(section("Ramas activas y commit de punta"))
    story.append(table(["Rama", "SHA"], [[rama, git("rev-parse", "--short", rama)] for rama in ramas_activas], [344, 159]))
    story.append(section("Autoría - git shortlog -sne HEAD"))
    story.append(Preformatted(shortlog, STYLES["code"]))

    story.append(PageBreak())
    story.extend(title("Organización del repositorio", "08  /  Trazabilidad del desarrollo individual"))
    story.append(section("git branch -a"))
    story.append(Preformatted(branches, STYLES["code"]))
    story.append(section("Integración hacia main - git log --first-parent main --oneline -n 9"))
    story.append(Preformatted(git("log", "--first-parent", "main", "--oneline", "-n", "9"), STYLES["code"]))
    story.append(Spacer(1, 15))
    story.append(p("La autoría registrada corresponde a Jade Sanchez. Las ramas sc-a-sanchez, sc-b-sanchez, sc-c-sanchez y sc-d-sanchez conservan seis commits de implementación cada una; feature/andinasalud-sanchez, develop y main completan la estructura de integración. El grafo y el first-parent permiten distinguir los commits directos históricos de las fusiones posteriores, sin alterar la autoría original.", "body"))
    story.append(section("Convención de commits"))
    story.append(p("Los mensajes del historial utilizan prefijos descriptivos en español: feat para funcionalidades, fix para correcciones, refactor para reorganización, style para interfaz y docs para documentación. El shortlog identifica las contribuciones bajo la autora individual del proyecto.", "body"))
    story.append(section("Referencia de versión"))
    story.append(p(f"Al generar este corte, la etiqueta <b>v1.0-unidad1</b> señala el commit <b>{tag}</b> y main señala <b>{head}</b>. La referencia publicada y vigente se consulta directamente en GitHub o con git rev-list -n 1 v1.0-unidad1.", "body"))

    story.append(PageBreak())
    story.extend(title("Síntesis del trabajo realizado", "09  /  Conclusiones"))
    story.append(p("La aplicación AndinaSalud implementa un flujo de gestión de citas con datos simulados en memoria y organiza su lógica bajo Clean Architecture y MVVM. El contrato del repositorio reside en dominio y la fuente simulada se concentra en data, lo que delimita la sustitución futura de datos sin trasladar reglas de negocio a la interfaz.", "body"))
    story.append(p("En la Parte I, las capturas Android E01-E13 y las pruebas documentan navegación, listado, búsqueda, filtros, detalle, validaciones, perfil y tema Material 3. RN-01 a RN-05 se localizan en casos de uso; las pruebas cubren fronteras temporales, límite de citas, duplicidad de horario, longitud del motivo y operaciones de solicitud y reprogramación.", "body"))
    story.append(p("En la Parte II, SC-A, SC-B, SC-C y SC-D se integran en dominio, datos y presentación según su alcance. Las ramas individuales y el historial de commits permiten inspeccionar su evolución sin atribuir contribuciones a personas distintas de la autora registrada.", "body"))
    story.append(section("Índice de evidencias"))
    story.append(table(["Grupo", "Contenido", "Localización"], [
        ["E01-E06", "Inicio, citas, detalle, solicitud, perfil y reprogramación.", "Apartado visual"],
        ["E07-E11", "Confirmación, errores, búsqueda, vacío e historial.", "Apartado visual"],
        ["E12-E13", "Tema oscuro en Perfil y Citas.", "Apartado visual"],
        ["Código y pruebas", "Modelos, casos de uso, ViewModels, DI y cuatro suites.", "Repositorio enlazado"],
        ["Git", "Grafo, ramas, shortlog y referencia de versión.", "Apartados 07 y 08"],
    ], [91, 269, 143]))
    story.append(Spacer(1, 15))
    story.append(notice("Reproducibilidad", "El README contiene las instrucciones de ejecución y este informe consigna los comandos de compilación y prueba. Las capturas se conservan como archivos fuente en evidencias/ para contrastarlas con el PDF."))

    doc.build(story, onFirstPage=page_frame, onLaterPages=page_frame)
    print(OUT)


if __name__ == "__main__":
    main()
