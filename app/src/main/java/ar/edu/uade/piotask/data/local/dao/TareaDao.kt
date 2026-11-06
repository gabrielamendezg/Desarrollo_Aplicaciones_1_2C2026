package ar.edu.uade.piotask.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ar.edu.uade.piotask.data.local.entity.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE completadaEn IS NULL ORDER BY id")
    fun observarActivas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    fun observarPorId(id: Long): Flow<TareaEntity?>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): TareaEntity?

    /** Devuelve el id nuevo al insertar y -1 al actualizar, por eso el repositorio resuelve el id final. */
    @Upsert
    suspend fun guardar(tarea: TareaEntity): Long

    @Query("DELETE FROM tareas WHERE id = :id")
    suspend fun eliminar(id: Long)
}
