package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.HOY
import ar.edu.uade.piotask.domain.ZONA
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.domain.model.TareasEnAtencion
import ar.edu.uade.piotask.domain.FakePreferenciasRepository
import ar.edu.uade.piotask.domain.FakeTareaRepository
import ar.edu.uade.piotask.domain.tarea
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import kotlin.random.Random

class ObtenerTareasEnAtencionUseCaseTest {

    private val tareaRepository = FakeTareaRepository()
    private val preferenciasRepository = FakePreferenciasRepository()
    private val tareas get() = tareaRepository.tareas
    private val criterios get() = preferenciasRepository.criterios

    private suspend fun obtener(hoy: LocalDate = HOY): TareasEnAtencion =
        ObtenerTareasEnAtencionUseCase(
            tareaRepository,
            preferenciasRepository,
            CalcularEstadoAtencionUseCase(),
            CalcularPuntajeAtencionUseCase(),
            Clock.fixed(hoy.atStartOfDay(ZONA).toInstant(), ZONA),
        )().first()

    private fun List<TareaEvaluada>.ids(): List<Long> = map { it.tarea.id }

    @Test
    fun `una tarea con avance reciente y deadline lejano va al Nido`() = runTest {
        tareas.value = listOf(tarea(id = 1, venceEnDias = 20), tarea(id = 2, venceEnDias = 2))

        val resultado = obtener()

        assertEquals(listOf(2L), resultado.visibles.ids())
        assertEquals(listOf(1L), resultado.enNido.ids())
    }

    @Test
    fun `una tarea sin deadline abandonada aparece en la Rama`() = runTest {
        tareas.value = listOf(tarea(id = 1, diasSinAvance = 18))

        assertEquals(listOf(1L), obtener().visibles.ids())
    }

    @Test
    fun `ordena por estado y dentro del estado por puntaje`() = runTest {
        tareas.value = listOf(
            tarea(id = 1, venceEnDias = 6),
            tarea(id = 2, venceEnDias = 2, progreso = 90),
            tarea(id = 3, venceEnDias = 3),
            tarea(id = 4, venceEnDias = 1),
        )

        assertEquals(listOf(4L, 2L, 3L, 1L), obtener().visibles.ids())
    }

    @Test
    fun `ante empate va primero el deadline mas cercano`() = runTest {
        tareas.value = listOf(
            tarea(id = 1, venceEnDias = -9),
            tarea(id = 2, venceEnDias = -8),
            tarea(id = 3, venceEnDias = -7),
        )

        assertEquals(listOf(1L, 2L, 3L), obtener().visibles.ids())
    }

    @Test
    fun `aplica el limite y deja el resto en mas tareas`() = runTest {
        criterios.value = Criterios(limiteVisibles = 3)
        tareas.value = (1L..5L).map { tarea(id = it, venceEnDias = it) }

        val resultado = obtener()

        assertEquals(listOf(1L, 2L, 3L), resultado.visibles.ids())
        assertEquals(listOf(4L, 5L), resultado.masTareas.ids())
    }

    @Test
    fun `descarta las tareas completadas`() = runTest {
        tareas.value = listOf(tarea(venceEnDias = 1).copy(completadaEn = HOY.atStartOfDay(ZONA).toInstant()))

        val resultado = obtener()

        assertTrue(resultado.visibles.isEmpty() && resultado.masTareas.isEmpty() && resultado.enNido.isEmpty())
    }

    @Test
    fun `al avanzar la fecha una tarea dormida despierta sin que cambien los datos`() = runTest {
        tareas.value = listOf(tarea(id = 1, venceEnDias = 8))

        assertEquals(listOf(1L), obtener().enNido.ids())
        assertEquals(listOf(1L), obtener(HOY.plusDays(1)).visibles.ids())
    }

    @Test
    fun `al guardar criterios nuevos se recalcula el reparto`() = runTest {
        tareas.value = listOf(tarea(id = 1, venceEnDias = 10))
        assertEquals(listOf(1L), obtener().enNido.ids())

        criterios.value = Criterios(ventanaDeadlineDias = 14)

        assertEquals(listOf(1L), obtener().visibles.ids())
    }

    @Test
    fun `ninguna tarea que reclama queda oculta en el Nido`() = runTest {
        val random = Random(2026)
        repeat(500) {
            criterios.value = Criterios(limiteVisibles = listOf(3, 5, 7).random(random))
            tareas.value = (1L..40L).map { id ->
                tarea(
                    id = id,
                    progreso = random.nextInt(0, 101),
                    venceEnDias = if (random.nextBoolean()) random.nextLong(-10, 30) else null,
                    diasSinAvance = random.nextLong(0, 60),
                    pospuestaDias = if (random.nextBoolean()) random.nextLong(-5, 15) else null,
                )
            }

            val hoy = HOY.plusDays(random.nextLong(0, 30))
            val resultado = obtener(hoy)

            assertEquals(40, resultado.visibles.size + resultado.masTareas.size + resultado.enNido.size)
            assertTrue(resultado.visibles.size <= criterios.value.limiteVisibles)
            resultado.enNido.forEach { (tarea, _) ->
                val diasRestantes = tarea.diasRestantes(hoy)
                val pospuesta = tarea.pospuestaHasta?.isAfter(hoy) == true
                assertFalse(diasRestantes != null && diasRestantes <= 2)
                assertFalse(!pospuesta && tarea.diasSinAvance(hoy, ZONA) >= 30)
            }
        }
    }
}
