package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Actividad
import kotlinx.coroutines.flow.Flow

/**
 * Solo lectura: las actividades se escriben a través de [TareaRepository] para que
 * queden en la misma transacción que el cambio de la tarea.
 */
interface ActividadRepository {
    fun observarPorTarea(tareaId: Long): Flow<List<Actividad>>
}
