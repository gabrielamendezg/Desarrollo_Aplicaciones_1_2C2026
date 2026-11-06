package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.TipoActividad
import ar.edu.uade.piotask.domain.repository.TareaRepository
import java.time.Clock
import javax.inject.Inject

/**
 * Registrar avance (RF03): guarda el nuevo porcentaje y reinicia el contador de días sin avance.
 */
class RegistrarAvanceUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(tareaId: Long, nuevoProgreso: Int): Resource<AccionDeshacible> = resultado {
        require(nuevoProgreso in 0..100) { "El progreso debe estar entre 0 y 100" }
        val anterior = tareaRepository.tareaPendiente(tareaId)
        val ahora = clock.instant()

        val actividadId = tareaRepository.guardarConActividad(
            tarea = anterior.copy(progreso = nuevoProgreso, ultimaActividad = ahora),
            actividad = Actividad(
                tareaId = tareaId,
                fecha = ahora,
                tipo = TipoActividad.AVANCE,
                progreso = nuevoProgreso,
            ),
        )
        AccionDeshacible(anterior, actividadId)
    }
}
