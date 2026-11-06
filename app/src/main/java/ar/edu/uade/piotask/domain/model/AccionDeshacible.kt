package ar.edu.uade.piotask.domain.model

/**
 * Lo mínimo para revertir una acción rápida: cómo estaba la tarea y qué actividad se registró.
 */
data class AccionDeshacible(
    val tareaAnterior: Tarea,
    val actividadId: Long,
)
