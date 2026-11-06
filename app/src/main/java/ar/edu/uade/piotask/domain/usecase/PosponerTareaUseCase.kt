package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.TipoActividad
import ar.edu.uade.piotask.domain.repository.TareaRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Posponer (RF03): la tarea va al Nido hasta la fecha elegida. No cuenta como avance,
 * así que `ultimaActividad` no cambia y al volver conserva sus días reales sin avance.
 */
class PosponerTareaUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(tareaId: Long, hasta: LocalDate): Resource<AccionDeshacible> = resultado {
        require(hasta.isAfter(LocalDate.now(clock))) { "La fecha para posponer tiene que ser posterior a hoy" }
        val anterior = tareaRepository.tareaPendiente(tareaId)

        val actividadId = tareaRepository.guardarConActividad(
            tarea = anterior.copy(pospuestaHasta = hasta),
            actividad = Actividad(
                tareaId = tareaId,
                fecha = clock.instant(),
                tipo = TipoActividad.POSPUESTA,
            ),
        )
        AccionDeshacible(anterior, actividadId)
    }
}
