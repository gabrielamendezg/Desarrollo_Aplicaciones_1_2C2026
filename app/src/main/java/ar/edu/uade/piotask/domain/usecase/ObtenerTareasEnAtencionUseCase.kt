package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.domain.model.TareasEnAtencion
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Reparte las tareas activas entre la Rama y el Nido, en el orden de docs/reglas-atencion.md.
 * Lo usan los ViewModels, el widget y el worker diario.
 */
class ObtenerTareasEnAtencionUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val calcularEstado: CalcularEstadoAtencionUseCase,
    private val calcularPuntaje: CalcularPuntajeAtencionUseCase,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<TareasEnAtencion> =
        combine(
            tareaRepository.observarActivas(),
            preferenciasRepository.observarCriterios(),
        ) { tareas, criterios -> repartir(tareas, criterios) }

    private fun repartir(tareas: List<Tarea>, criterios: Criterios): TareasEnAtencion {
        val hoy = LocalDate.now(clock)
        val (enAtencion, enNido) = tareas
            .filter { it.completadaEn == null }
            .map { tarea ->
                TareaEvaluada(
                    tarea = tarea,
                    evaluacion = calcularEstado(tarea, criterios, hoy, clock.zone),
                    puntaje = calcularPuntaje(tarea, criterios, hoy, clock.zone),
                )
            }
            .sortedWith(ORDEN)
            .partition { it.evaluacion.requiereAtencion }

        return TareasEnAtencion(
            visibles = enAtencion.take(criterios.limiteVisibles),
            masTareas = enAtencion.drop(criterios.limiteVisibles),
            enNido = enNido,
        )
    }

    private companion object {
        val ORDEN: Comparator<TareaEvaluada> = compareByDescending<TareaEvaluada> { it.evaluacion.estado }
            .thenByDescending { it.puntaje }
            .thenBy(nullsLast()) { it.tarea.deadline }
            .thenBy { it.tarea.id }
    }
}
