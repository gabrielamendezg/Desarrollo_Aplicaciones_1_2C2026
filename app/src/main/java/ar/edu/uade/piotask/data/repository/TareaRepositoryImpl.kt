package ar.edu.uade.piotask.data.repository

import androidx.room.withTransaction
import ar.edu.uade.piotask.data.local.AppDatabase
import ar.edu.uade.piotask.data.local.dao.ActividadDao
import ar.edu.uade.piotask.data.local.dao.TareaDao
import ar.edu.uade.piotask.data.mapper.toDomain
import ar.edu.uade.piotask.data.mapper.toEntity
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TareaRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val tareaDao: TareaDao,
    private val actividadDao: ActividadDao,
) : TareaRepository {

    override fun observarActivas(): Flow<List<Tarea>> =
        tareaDao.observarActivas().map { lista -> lista.map { it.toDomain() } }

    override fun observarPorId(id: Long): Flow<Tarea?> =
        tareaDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun obtenerPorId(id: Long): Tarea? = tareaDao.obtenerPorId(id)?.toDomain()

    override suspend fun guardar(tarea: Tarea): Long = guardarEntidad(tarea)

    override suspend fun eliminar(id: Long) = tareaDao.eliminar(id)

    override suspend fun guardarConActividad(tarea: Tarea, actividad: Actividad): Long =
        database.withTransaction {
            guardarEntidad(tarea)
            actividadDao.insertar(actividad.toEntity())
        }

    override suspend fun revertir(tareaAnterior: Tarea, actividadId: Long) =
        database.withTransaction {
            tareaDao.guardar(tareaAnterior.toEntity())
            actividadDao.eliminar(actividadId)
        }

    /** `@Upsert` devuelve -1 al actualizar; en ese caso el id es el que ya tenía la tarea. */
    private suspend fun guardarEntidad(tarea: Tarea): Long {
        val resultado = tareaDao.guardar(tarea.toEntity())
        return if (resultado == -1L) tarea.id else resultado
    }
}
