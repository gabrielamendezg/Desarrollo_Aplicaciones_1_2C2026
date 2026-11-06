package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.TipoActividad
import ar.edu.uade.piotask.domain.repository.TareaRepository
import java.time.Clock
import javax.inject.Inject

/**
 * Completar (RF03): la tarea sale de todas las vistas activas.
 */
class CompletarTareaUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(tareaId: Long): Resource<AccionDeshacible> = resultado {
        val anterior = tareaRepository.tareaPendiente(tareaId)
        val ahora = clock.instant()

        val actividadId = tareaRepository.guardarConActividad(
            tarea = anterior.copy(completadaEn = ahora, ultimaActividad = ahora),
            actividad = Actividad(
                tareaId = tareaId,
                fecha = ahora,
                tipo = TipoActividad.COMPLETADA,
                progreso = anterior.progreso,
            ),
        )
        AccionDeshacible(anterior, actividadId)
    }
}
