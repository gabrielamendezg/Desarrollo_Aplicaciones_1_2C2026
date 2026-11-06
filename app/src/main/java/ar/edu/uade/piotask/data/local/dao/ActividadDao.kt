package ar.edu.uade.piotask.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ar.edu.uade.piotask.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {
    @Query("SELECT * FROM actividades WHERE tareaId = :tareaId ORDER BY fecha DESC, id DESC")
    fun observarPorTarea(tareaId: Long): Flow<List<ActividadEntity>>

    @Insert
    suspend fun insertar(actividad: ActividadEntity): Long

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun eliminar(id: Long)
}
