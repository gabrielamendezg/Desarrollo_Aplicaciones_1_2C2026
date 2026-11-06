package ar.edu.uade.piotask.data.repository

import ar.edu.uade.piotask.data.local.dao.ActividadDao
import ar.edu.uade.piotask.data.mapper.toDomain
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.repository.ActividadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActividadRepositoryImpl @Inject constructor(
    private val actividadDao: ActividadDao,
) : ActividadRepository {

    override fun observarPorTarea(tareaId: Long): Flow<List<Actividad>> =
        actividadDao.observarPorTarea(tareaId).map { lista -> lista.map { it.toDomain() } }
}
