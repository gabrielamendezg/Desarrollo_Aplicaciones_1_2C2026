package ar.edu.uade.piotask.domain.model

import java.time.LocalDate

/**
 * Por qué una tarea está donde está. La capa de presentación lo convierte en texto.
 */
sealed interface MotivoAtencion {
    data class VenceEn(val dias: Long) : MotivoAtencion
    data class Vencida(val dias: Long) : MotivoAtencion
    data class SinAvance(val dias: Long) : MotivoAtencion
    data class Pospuesta(val hasta: LocalDate) : MotivoAtencion
    data object Completada : MotivoAtencion
}

data class EvaluacionAtencion(
    val estado: EstadoAtencion,
    val motivo: MotivoAtencion,
    val pospuesta: Boolean = false,
) {
    /** Las tareas que requieren atención van a la Rama; el resto descansa en el Nido. */
    val requiereAtencion: Boolean
        get() = !pospuesta && (estado == EstadoAtencion.DESPIERTO || estado == EstadoAtencion.RECLAMANDO)
}
