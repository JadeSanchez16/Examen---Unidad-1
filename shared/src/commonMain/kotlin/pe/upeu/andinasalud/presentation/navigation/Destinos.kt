package pe.upeu.andinasalud.presentation.navigation

object Destinos {
    const val INICIO = "inicio"
    const val CITAS = "citas"
    const val PERFIL = "perfil"
    const val AJUSTES = "ajustes"
    const val SOLICITUD = "solicitud"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: String) = "detalle/$id"
}
