package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.TareaRepository

/** La tarea con ese id, que tiene que existir y no estar completada. */
internal suspend fun TareaRepository.tareaPendiente(id: Long): Tarea {
    val tarea = obtenerPorId(id) ?: throw IllegalArgumentException("La tarea no existe")
    require(tarea.completadaEn == null) { "La tarea ya está completada" }
    return tarea
}
