package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.FakePreferenciasRepository
import ar.edu.uade.piotask.domain.FakeTareaRepository
import ar.edu.uade.piotask.domain.HOY
import ar.edu.uade.piotask.domain.ZONA
import ar.edu.uade.piotask.domain.model.TipoActividad
import ar.edu.uade.piotask.domain.relojFijo
import ar.edu.uade.piotask.domain.tarea
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccionesSobreTareaUseCasesTest {

    private val repo = FakeTareaRepository()
    private val prefs = FakePreferenciasRepository()
    private val reloj = relojFijo()

    private val guardar = GuardarTareaUseCase(repo, prefs, reloj)
    private val registrarAvance = RegistrarAvanceUseCase(repo, reloj)
    private val posponer = PosponerTareaUseCase(repo, reloj)
    private val completar = CompletarTareaUseCase(repo, reloj)
    private val deshacer = DeshacerAccionUseCase(repo)

    private fun <T> Resource<T>.datos(): T = (this as Resource.Success).data

    @Test
    fun `guardar una tarea nueva fija creacion y ultima actividad al momento actual y recuerda la categoria`() = runTest {
        val id = guardar(tarea(id = 0).copy(titulo = "  Estudiar  ", categoriaId = 7)).datos()

        val guardada = repo.obtenerPorId(id)!!
        assertEquals("Estudiar", guardada.titulo)
        assertEquals(reloj.instant(), guardada.creadaEn)
        assertEquals(reloj.instant(), guardada.ultimaActividad)
        assertEquals(7L, prefs.ultimaCategoria.value)
    }

    @Test
    fun `editar una tarea no cuenta como avance`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1, diasSinAvance = 10))
        val original = repo.obtenerPorId(1)!!

        guardar(original.copy(titulo = "Nuevo titulo"))

        assertEquals(original.ultimaActividad, repo.obtenerPorId(1)!!.ultimaActividad)
    }

    @Test
    fun `guardar rechaza titulo vacio, progreso fuera de rango y ventanas menores a un dia`() = runTest {
        assertTrue(guardar(tarea(id = 0).copy(titulo = "  ")) is Resource.Error)
        assertTrue(guardar(tarea(id = 0, progreso = 101)) is Resource.Error)
        assertTrue(guardar(tarea(id = 0, ventanaDeadlineDias = 0)) is Resource.Error)
        assertTrue(guardar(tarea(id = 0, ventanaInactividadDias = 0)) is Resource.Error)
        assertTrue(repo.tareas.value.isEmpty())
    }

    @Test
    fun `registrar avance guarda el progreso, reinicia los dias sin avance y deja una actividad`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1, diasSinAvance = 20))

        registrarAvance(1, 60).datos()

        val t = repo.obtenerPorId(1)!!
        assertEquals(60, t.progreso)
        assertEquals(0L, t.diasSinAvance(HOY, ZONA))
        assertEquals(TipoActividad.AVANCE, repo.actividades.single().tipo)
        assertEquals(60, repo.actividades.single().progreso)
    }

    @Test
    fun `registrar avance rechaza porcentajes invalidos y tareas inexistentes`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1))

        assertTrue(registrarAvance(1, 120) is Resource.Error)
        assertTrue(registrarAvance(1, -1) is Resource.Error)
        assertTrue(registrarAvance(99, 10) is Resource.Error)
        assertTrue(repo.actividades.isEmpty())
    }

    @Test
    fun `posponer no cuenta como avance y conserva los dias reales sin avance`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1, diasSinAvance = 15))

        posponer(1, HOY.plusDays(3)).datos()

        val t = repo.obtenerPorId(1)!!
        assertEquals(HOY.plusDays(3), t.pospuestaHasta)
        assertEquals(15L, t.diasSinAvance(HOY, ZONA))
        assertEquals(TipoActividad.POSPUESTA, repo.actividades.single().tipo)
    }

    @Test
    fun `posponer exige una fecha posterior a hoy`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1))

        assertTrue(posponer(1, HOY) is Resource.Error)
        assertTrue(posponer(1, HOY.minusDays(1)) is Resource.Error)
    }

    @Test
    fun `completar saca la tarea de las activas`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1), tarea(id = 2))

        completar(1).datos()

        assertNotNull(repo.obtenerPorId(1)!!.completadaEn)
        assertEquals(listOf(2L), repo.tareas.value.filter { it.completadaEn == null }.map { it.id })
        assertEquals(TipoActividad.COMPLETADA, repo.actividades.single().tipo)
    }

    @Test
    fun `no se puede completar ni posponer una tarea ya completada`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1))
        completar(1).datos()

        assertTrue(completar(1) is Resource.Error)
        assertTrue(posponer(1, HOY.plusDays(2)) is Resource.Error)
    }

    @Test
    fun `deshacer completar devuelve la tarea a como estaba y borra la actividad`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1, progreso = 40, diasSinAvance = 5))
        val antes = repo.obtenerPorId(1)!!

        val accion = completar(1).datos()
        deshacer(accion).datos()

        assertEquals(antes, repo.obtenerPorId(1))
        assertTrue(repo.actividades.isEmpty())
    }

    @Test
    fun `deshacer posponer quita la posposicion`() = runTest {
        repo.tareas.value = listOf(tarea(id = 1))

        val accion = posponer(1, HOY.plusDays(7)).datos()
        deshacer(accion).datos()

        assertNull(repo.obtenerPorId(1)!!.pospuestaHasta)
        assertTrue(repo.actividades.isEmpty())
    }
}
