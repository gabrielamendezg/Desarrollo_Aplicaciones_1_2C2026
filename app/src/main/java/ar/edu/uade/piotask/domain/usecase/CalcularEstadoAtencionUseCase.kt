package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.EstadoAtencion
import ar.edu.uade.piotask.domain.model.EvaluacionAtencion
import ar.edu.uade.piotask.domain.model.MotivoAtencion
import ar.edu.uade.piotask.domain.model.Tarea
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Calcula el estado de atención con los dos relojes de docs/reglas-atencion.md.
 * El estado no se persiste: siempre se deriva de la tarea y la fecha actual.
 */
class CalcularEstadoAtencionUseCase @Inject constructor() {

    operator fun invoke(
        tarea: Tarea,
        criterios: Criterios,
        hoy: LocalDate,
        zona: ZoneId,
    ): EvaluacionAtencion {
        if (tarea.completadaEn != null) {
            return EvaluacionAtencion(EstadoAtencion.COMPLETADA, MotivoAtencion.Completada)
        }

        val porDeadline = estadoPorDeadline(tarea, criterios, hoy)
        val porInactividad = estadoPorInactividad(tarea, criterios, hoy, zona)

        val pospuestaHasta = tarea.pospuestaHasta
        if (pospuestaHasta != null && hoy < pospuestaHasta && porDeadline != EstadoAtencion.RECLAMANDO) {
            return EvaluacionAtencion(
                estado = EstadoAtencion.DORMIDO,
                motivo = MotivoAtencion.Pospuesta(pospuestaHasta),
                pospuesta = true,
            )
        }

        return EvaluacionAtencion(
            estado = maxOf(porDeadline, porInactividad),
            motivo = motivo(tarea, porDeadline, porInactividad, hoy, zona),
        )
    }

    private fun estadoPorDeadline(tarea: Tarea, criterios: Criterios, hoy: LocalDate): EstadoAtencion {
        val restantes = tarea.diasRestantes(hoy) ?: return EstadoAtencion.DORMIDO
        val ventana = tarea.ventanaDeadline(criterios)
        val diaReclamo = ventana * 3 / 10
        return when {
            restantes <= diaReclamo -> EstadoAtencion.RECLAMANDO
            restantes <= ventana -> EstadoAtencion.DESPIERTO
            else -> EstadoAtencion.DORMIDO
        }
    }

    private fun estadoPorInactividad(
        tarea: Tarea,
        criterios: Criterios,
        hoy: LocalDate,
        zona: ZoneId,
    ): EstadoAtencion {
        val dias = tarea.diasSinAvance(hoy, zona)
        val ventana = tarea.ventanaInactividad(criterios)
        val diaDespierto = (ventana * 4 + 9) / 10
        return when {
            dias >= ventana -> EstadoAtencion.RECLAMANDO
            dias >= diaDespierto -> EstadoAtencion.DESPIERTO
            else -> EstadoAtencion.DORMIDO
        }
    }

    private fun motivo(
        tarea: Tarea,
        porDeadline: EstadoAtencion,
        porInactividad: EstadoAtencion,
        hoy: LocalDate,
        zona: ZoneId,
    ): MotivoAtencion {
        val restantes = tarea.diasRestantes(hoy)
        return when {
            restantes == null || porInactividad > porDeadline ->
                MotivoAtencion.SinAvance(tarea.diasSinAvance(hoy, zona))
            restantes < 0 -> MotivoAtencion.Vencida(-restantes)
            else -> MotivoAtencion.VenceEn(restantes)
        }
    }
}
