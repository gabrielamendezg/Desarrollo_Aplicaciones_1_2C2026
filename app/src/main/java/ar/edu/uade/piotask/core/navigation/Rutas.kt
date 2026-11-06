package ar.edu.uade.piotask.core.navigation

object Rutas {
    const val RAMA = "rama"
    const val NIDO = "nido"
    const val AJUSTES = "ajustes"
    const val NUEVA_TAREA = "nueva_tarea"

    const val ARG_TAREA_ID = "tareaId"
    const val DETALLE = "detalle/{$ARG_TAREA_ID}"
    const val EDITAR = "editar/{$ARG_TAREA_ID}"

    fun detalle(tareaId: Long) = "detalle/$tareaId"
    fun editar(tareaId: Long) = "editar/$tareaId"
}
