package ar.edu.uade.piotask.data.repository

import androidx.room.withTransaction
import ar.edu.uade.piotask.data.local.AppDatabase
import ar.edu.uade.piotask.data.local.dao.CategoriaDao
import ar.edu.uade.piotask.data.mapper.toDomain
import ar.edu.uade.piotask.data.mapper.toEntity
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoriaRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val categoriaDao: CategoriaDao,
) : CategoriaRepository {

    override fun observarTodas(): Flow<List<Categoria>> =
        categoriaDao.observarTodas().map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtenerPorId(id: Long): Categoria? = categoriaDao.obtenerPorId(id)?.toDomain()

    override suspend fun guardar(categoria: Categoria): Long {
        val resultado = categoriaDao.guardar(categoria.toEntity())
        return if (resultado == -1L) categoria.id else resultado
    }

    override suspend fun cantidadDeTareas(categoriaId: Long): Int = categoriaDao.cantidadDeTareas(categoriaId)

    override suspend fun reasignarYEliminar(categoriaId: Long, destinoId: Long?) =
        database.withTransaction {
            if (destinoId != null) categoriaDao.reasignarTareas(categoriaId, destinoId)
            categoriaDao.eliminar(categoriaId)
        }
}
