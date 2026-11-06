package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import javax.inject.Inject

/**
 * Una categoría con tareas no se puede eliminar hasta reasignarlas a otra (sección 13 del TPO).
 */
class EliminarCategoriaUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    suspend operator fun invoke(categoriaId: Long, reasignarA: Long? = null): Resource<Unit> = resultado {
        val cantidad = categoriaRepository.cantidadDeTareas(categoriaId)
        if (cantidad > 0) {
            require(reasignarA != null) {
                "La categoría tiene $cantidad tareas: reasignalas a otra categoría antes de eliminarla"
            }
            require(reasignarA != categoriaId) { "Elegí una categoría distinta para reasignar las tareas" }
            requireNotNull(categoriaRepository.obtenerPorId(reasignarA)) { "La categoría de destino no existe" }
        }
        categoriaRepository.reasignarYEliminar(categoriaId, reasignarA)
    }
}
