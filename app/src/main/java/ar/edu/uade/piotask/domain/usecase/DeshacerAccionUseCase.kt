package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.repository.TareaRepository
import javax.inject.Inject

/**
 * Deshacer (snackbar de 5 s): devuelve la tarea a como estaba y borra la actividad registrada.
 */
class DeshacerAccionUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    suspend operator fun invoke(accion: AccionDeshacible): Resource<Unit> = resultado {
        tareaRepository.revertir(accion.tareaAnterior, accion.actividadId)
    }
}
