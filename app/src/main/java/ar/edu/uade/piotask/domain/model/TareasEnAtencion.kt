package ar.edu.uade.piotask.domain.model

data class TareaEvaluada(
    val tarea: Tarea,
    val evaluacion: EvaluacionAtencion,
    val puntaje: Double,
)

/**
 * Reparto de las tareas activas entre la Rama, el indicador "+N" y el Nido.
 */
data class TareasEnAtencion(
    val visibles: List<TareaEvaluada>,
    val masTareas: List<TareaEvaluada>,
    val enNido: List<TareaEvaluada>,
)
