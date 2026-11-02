package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Criterios
import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    fun observarCriterios(): Flow<Criterios>
}
