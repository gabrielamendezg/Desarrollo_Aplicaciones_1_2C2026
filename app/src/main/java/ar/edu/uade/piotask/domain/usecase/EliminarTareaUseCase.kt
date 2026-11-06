package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.repository.TareaRepository
import javax.inject.Inject

class EliminarTareaUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    suspend operator fun invoke(tareaId: Long): Resource<Unit> = resultado {
        tareaRepository.eliminar(tareaId)
    }
}
