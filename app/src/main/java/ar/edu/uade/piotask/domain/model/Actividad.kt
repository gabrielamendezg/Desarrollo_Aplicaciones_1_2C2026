package ar.edu.uade.piotask.domain.model

import java.time.Instant

enum class TipoActividad { AVANCE, POSPUESTA, COMPLETADA }

/**
 * Registro de algo que el usuario hizo sobre una tarea. Sirve de historial y para deshacer.
 */
data class Actividad(
    val id: Long = 0,
    val tareaId: Long,
    val fecha: Instant,
    val tipo: TipoActividad,
    val progreso: Int? = null,
)
