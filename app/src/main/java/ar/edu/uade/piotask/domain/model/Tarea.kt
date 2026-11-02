package ar.edu.uade.piotask.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class Tarea(
    val id: Long = 0,
    val titulo: String,
    val categoriaId: Long,
    val progreso: Int = 0,
    val deadline: LocalDate? = null,
    val pospuestaHasta: LocalDate? = null,
    val ventanaDeadlineDias: Int? = null,
    val ventanaInactividadDias: Int? = null,
    val creadaEn: Instant,
    val ultimaActividad: Instant,
    val completadaEn: Instant? = null,
) {
    fun ventanaDeadline(criterios: Criterios): Int =
        ventanaDeadlineDias ?: criterios.ventanaDeadlineDias

    fun ventanaInactividad(criterios: Criterios): Int =
        ventanaInactividadDias ?: criterios.ventanaInactividadDias

    /** Días de calendario hasta el deadline; negativo si está vencida. */
    fun diasRestantes(hoy: LocalDate): Long? =
        deadline?.let { ChronoUnit.DAYS.between(hoy, it) }

    /** Días de calendario sin avance; nunca negativo aunque la fecha del sistema retroceda. */
    fun diasSinAvance(hoy: LocalDate, zona: ZoneId): Long =
        ChronoUnit.DAYS.between(ultimaActividad.atZone(zona).toLocalDate(), hoy).coerceAtLeast(0)
}
