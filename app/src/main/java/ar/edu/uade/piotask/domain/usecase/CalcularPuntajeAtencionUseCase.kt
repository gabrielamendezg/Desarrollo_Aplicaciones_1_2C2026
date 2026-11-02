package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.Tarea
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Puntaje de 0 a 100 que ordena las tareas dentro de un mismo estado.
 * Pesos pendientes de validación: ver docs/reglas-atencion.md.
 */
class CalcularPuntajeAtencionUseCase @Inject constructor() {

    operator fun invoke(
        tarea: Tarea,
        criterios: Criterios,
        hoy: LocalDate,
        zona: ZoneId,
    ): Double {
        val presionDeadline = presionDeadline(tarea, criterios, hoy)
        val presionInactividad = (tarea.diasSinAvance(hoy, zona).toDouble() / tarea.ventanaInactividad(criterios))
            .coerceIn(0.0, 1.0)
        val trabajoRestante = (100 - tarea.progreso) / 100.0

        return 100 * (
            PESO_DEADLINE * presionDeadline * (BASE_TRABAJO + (1 - BASE_TRABAJO) * trabajoRestante) +
                PESO_INACTIVIDAD * presionInactividad
            )
    }

    private fun presionDeadline(tarea: Tarea, criterios: Criterios, hoy: LocalDate): Double {
        val restantes = tarea.diasRestantes(hoy) ?: return 0.0
        val ventana = tarea.ventanaDeadline(criterios)
        return ((ventana - restantes).toDouble() / ventana).coerceIn(0.0, 1.0)
    }

    private companion object {
        const val PESO_DEADLINE = 0.6
        const val PESO_INACTIVIDAD = 0.4
        const val BASE_TRABAJO = 0.5
    }
}
