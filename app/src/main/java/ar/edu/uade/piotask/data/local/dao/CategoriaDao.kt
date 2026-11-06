package ar.edu.uade.piotask.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ar.edu.uade.piotask.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY id")
    fun observarTodas(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CategoriaEntity?

    @Upsert
    suspend fun guardar(categoria: CategoriaEntity): Long

    @Query("SELECT COUNT(*) FROM tareas WHERE categoriaId = :categoriaId")
    suspend fun cantidadDeTareas(categoriaId: Long): Int

    @Query("UPDATE tareas SET categoriaId = :destinoId WHERE categoriaId = :origenId")
    suspend fun reasignarTareas(origenId: Long, destinoId: Long)

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun eliminar(id: Long)
}
