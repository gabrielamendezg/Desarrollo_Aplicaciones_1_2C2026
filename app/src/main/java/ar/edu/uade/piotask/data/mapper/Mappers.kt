package ar.edu.uade.piotask.data.mapper

import ar.edu.uade.piotask.data.local.entity.ActividadEntity
import ar.edu.uade.piotask.data.local.entity.CategoriaEntity
import ar.edu.uade.piotask.data.local.entity.TareaEntity
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Tarea

fun TareaEntity.toDomain() = Tarea(
    id = id,
    titulo = titulo,
    categoriaId = categoriaId,
    progreso = progreso,
    deadline = deadline,
    pospuestaHasta = pospuestaHasta,
    ventanaDeadlineDias = ventanaDeadlineDias,
    ventanaInactividadDias = ventanaInactividadDias,
    creadaEn = creadaEn,
    ultimaActividad = ultimaActividad,
    completadaEn = completadaEn,
)

fun Tarea.toEntity() = TareaEntity(
    id = id,
    titulo = titulo,
    categoriaId = categoriaId,
    progreso = progreso,
    deadline = deadline,
    pospuestaHasta = pospuestaHasta,
    ventanaDeadlineDias = ventanaDeadlineDias,
    ventanaInactividadDias = ventanaInactividadDias,
    creadaEn = creadaEn,
    ultimaActividad = ultimaActividad,
    completadaEn = completadaEn,
)

fun CategoriaEntity.toDomain() = Categoria(id = id, nombre = nombre, colorHex = colorHex)

fun Categoria.toEntity() = CategoriaEntity(id = id, nombre = nombre, colorHex = colorHex)

fun ActividadEntity.toDomain() = Actividad(
    id = id,
    tareaId = tareaId,
    fecha = fecha,
    tipo = tipo,
    progreso = progreso,
)

fun Actividad.toEntity() = ActividadEntity(
    id = id,
    tareaId = tareaId,
    fecha = fecha,
    tipo = tipo,
    progreso = progreso,
)
