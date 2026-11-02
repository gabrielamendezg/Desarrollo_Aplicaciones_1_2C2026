package ar.edu.uade.piotask.domain

import ar.edu.uade.piotask.domain.model.Tarea
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

val ZONA: ZoneId = ZoneOffset.UTC
val HOY: LocalDate = LocalDate.of(2026, 11, 2)

fun tarea(
    id: Long = 1,
    progreso: Int = 0,
    venceEnDias: Long? = null,
    diasSinAvance: Long = 0,
    pospuestaDias: Long? = null,
    ventanaDeadlineDias: Int? = null,
    ventanaInactividadDias: Int? = null,
): Tarea {
    val ultimaActividad = HOY.minusDays(diasSinAvance).atStartOfDay(ZONA).toInstant()
    return Tarea(
        id = id,
        titulo = "Tarea $id",
        categoriaId = 1,
        progreso = progreso,
        deadline = venceEnDias?.let(HOY::plusDays),
        pospuestaHasta = pospuestaDias?.let(HOY::plusDays),
        ventanaDeadlineDias = ventanaDeadlineDias,
        ventanaInactividadDias = ventanaInactividadDias,
        creadaEn = ultimaActividad,
        ultimaActividad = ultimaActividad,
    )
}
