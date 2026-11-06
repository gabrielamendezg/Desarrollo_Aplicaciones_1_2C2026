package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import javax.inject.Inject

/**
 * RF04: guarda los criterios globales. Los estados se recalculan solos porque el flujo de
 * criterios alimenta a [ObtenerTareasEnAtencionUseCase].
 */
class ActualizarCriteriosUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
) {
    suspend operator fun invoke(criterios: Criterios): Resource<Unit> = resultado {
        require(criterios.ventanaDeadlineDias >= 1) { "La ventana de deadline es de al menos 1 día" }
        require(criterios.ventanaInactividadDias >= 1) { "La ventana de inactividad es de al menos 1 día" }
        require(criterios.limiteVisibles in LIMITES_PERMITIDOS) { "Las tareas visibles pueden ser 3, 5 o 7" }
        preferenciasRepository.actualizarCriterios(criterios)
    }

    companion object {
        val LIMITES_PERMITIDOS = listOf(3, 5, 7)
    }
}
