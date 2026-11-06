package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    fun observarActivas(): Flow<List<Tarea>>

    fun observarPorId(id: Long): Flow<Tarea?>

    suspend fun obtenerPorId(id: Long): Tarea?

    /** Inserta si `id == 0`, si no actualiza. Devuelve el id de la tarea. */
    suspend fun guardar(tarea: Tarea): Long

    suspend fun eliminar(id: Long)

    /** Guarda la tarea y registra la actividad en una sola transacción. Devuelve el id de la actividad. */
    suspend fun guardarConActividad(tarea: Tarea, actividad: Actividad): Long

    /** Restaura la tarea anterior y borra la actividad, en una sola transacción. */
    suspend fun revertir(tareaAnterior: Tarea, actividadId: Long)
}
