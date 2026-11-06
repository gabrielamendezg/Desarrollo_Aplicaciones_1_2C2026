package ar.edu.uade.piotask.domain.repository

import ar.edu.uade.piotask.domain.model.Criterios
import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    fun observarCriterios(): Flow<Criterios>

    suspend fun actualizarCriterios(criterios: Criterios)

    fun observarOnboardingVisto(): Flow<Boolean>

    suspend fun marcarOnboardingVisto()

    fun observarUltimaCategoriaId(): Flow<Long?>

    suspend fun guardarUltimaCategoriaId(id: Long)
}
