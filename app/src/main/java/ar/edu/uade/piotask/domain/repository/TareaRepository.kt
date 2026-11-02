package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    fun observarActivas(): Flow<List<Tarea>>
}
