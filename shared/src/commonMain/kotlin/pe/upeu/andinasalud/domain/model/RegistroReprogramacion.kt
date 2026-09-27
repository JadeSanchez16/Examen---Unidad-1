package pe.upeu.andinasalud.domain.model

import kotlinx.datetime.LocalDateTime

data class RegistroReprogramacion(val anterior: LocalDateTime, val nueva: LocalDateTime)
