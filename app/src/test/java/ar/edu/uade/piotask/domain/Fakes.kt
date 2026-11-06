package ar.edu.uade.piotask.domain

import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.ActividadRepository
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Clock

fun relojFijo(hoy: java.time.LocalDate = HOY): Clock = Clock.fixed(hoy.atStartOfDay(ZONA).toInstant(), ZONA)

class FakeTareaRepository : TareaRepository {
    val tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val actividades = mutableListOf<Actividad>()
    private var siguienteId = 100L
    private var siguienteActividadId = 1L

    override fun observarActivas(): Flow<List<Tarea>> = tareas.map { lista -> lista.filter { it.completadaEn == null } }

    override fun observarPorId(id: Long): Flow<Tarea?> = tareas.map { lista -> lista.firstOrNull { it.id == id } }

    override suspend fun obtenerPorId(id: Long): Tarea? = tareas.value.firstOrNull { it.id == id }

    override suspend fun guardar(tarea: Tarea): Long {
        val id = if (tarea.id == 0L) siguienteId++ else tarea.id
        val guardada = tarea.copy(id = id)
        tareas.value = tareas.value.filterNot { it.id == id } + guardada
        return id
    }

    override suspend fun eliminar(id: Long) {
        tareas.value = tareas.value.filterNot { it.id == id }
        actividades.removeAll { it.tareaId == id }
    }

    override suspend fun guardarConActividad(tarea: Tarea, actividad: Actividad): Long {
        guardar(tarea)
        val id = siguienteActividadId++
        actividades += actividad.copy(id = id)
        return id
    }

    override suspend fun revertir(tareaAnterior: Tarea, actividadId: Long) {
        guardar(tareaAnterior)
        actividades.removeAll { it.id == actividadId }
    }
}

class FakeCategoriaRepository(
    inicial: List<Categoria> = emptyList(),
    private val tareas: FakeTareaRepository = FakeTareaRepository(),
) : CategoriaRepository {
    val categorias = MutableStateFlow(inicial)
    private var siguienteId = 100L

    override fun observarTodas(): Flow<List<Categoria>> = categorias

    override suspend fun obtenerPorId(id: Long): Categoria? = categorias.value.firstOrNull { it.id == id }

    override suspend fun guardar(categoria: Categoria): Long {
        val id = if (categoria.id == 0L) siguienteId++ else categoria.id
        categorias.value = categorias.value.filterNot { it.id == id } + categoria.copy(id = id)
        return id
    }

    override suspend fun cantidadDeTareas(categoriaId: Long): Int =
        tareas.tareas.value.count { it.categoriaId == categoriaId }

    override suspend fun reasignarYEliminar(categoriaId: Long, destinoId: Long?) {
        if (destinoId != null) {
            tareas.tareas.value = tareas.tareas.value.map {
                if (it.categoriaId == categoriaId) it.copy(categoriaId = destinoId) else it
            }
        }
        categorias.value = categorias.value.filterNot { it.id == categoriaId }
    }
}

class FakeActividadRepository(private val tareas: FakeTareaRepository) : ActividadRepository {
    override fun observarPorTarea(tareaId: Long): Flow<List<Actividad>> =
        tareas.tareas.map { tareas.actividades.filter { it.tareaId == tareaId } }
}

class FakePreferenciasRepository : PreferenciasRepository {
    val criterios = MutableStateFlow(Criterios())
    val onboardingVisto = MutableStateFlow(false)
    val ultimaCategoria = MutableStateFlow<Long?>(null)

    override fun observarCriterios(): Flow<Criterios> = criterios

    override suspend fun actualizarCriterios(criterios: Criterios) {
        this.criterios.value = criterios
    }

    override fun observarOnboardingVisto(): Flow<Boolean> = onboardingVisto

    override suspend fun marcarOnboardingVisto() {
        onboardingVisto.value = true
    }

    override fun observarUltimaCategoriaId(): Flow<Long?> = ultimaCategoria

    override suspend fun guardarUltimaCategoriaId(id: Long) {
        ultimaCategoria.value = id
    }
}
