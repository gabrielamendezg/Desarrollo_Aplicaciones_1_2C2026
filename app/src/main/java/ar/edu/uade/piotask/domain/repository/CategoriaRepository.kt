package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {
    fun observarTodas(): Flow<List<Categoria>>

    suspend fun obtenerPorId(id: Long): Categoria?

    /** Inserta si `id == 0`, si no actualiza. Devuelve el id de la categoría. */
    suspend fun guardar(categoria: Categoria): Long

    /** Cantidad de tareas, activas o completadas, que pertenecen a la categoría. */
    suspend fun cantidadDeTareas(categoriaId: Long): Int

    /** Mueve las tareas de una categoría a otra y borra la primera, en una sola transacción. */
    suspend fun reasignarYEliminar(categoriaId: Long, destinoId: Long?)
}
