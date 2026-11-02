package ar.edu.uade.piotask.domain.model

/**
 * Criterios globales de atención, configurables desde Ajustes.
 */
data class Criterios(
    val ventanaDeadlineDias: Int = 7,
    val ventanaInactividadDias: Int = 30,
    val limiteVisibles: Int = 5,
)
