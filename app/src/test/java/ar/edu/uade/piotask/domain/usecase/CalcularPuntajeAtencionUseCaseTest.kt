package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.HOY
import ar.edu.uade.piotask.domain.ZONA
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.tarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcularPuntajeAtencionUseCaseTest {

    private val calcular = CalcularPuntajeAtencionUseCase()

    private fun puntaje(tarea: Tarea): Double = calcular(tarea, Criterios(), HOY, ZONA)

    @Test
    fun `una tarea sin presion puntua cero`() {
        assertEquals(0.0, puntaje(tarea()), 0.001)
        assertEquals(0.0, puntaje(tarea(venceEnDias = 30)), 0.001)
    }

    @Test
    fun `reproduce el ejemplo documentado`() {
        assertEquals(45.2, puntaje(tarea(progreso = 20, venceEnDias = 2, diasSinAvance = 5)), 0.1)
        assertEquals(30.2, puntaje(tarea(progreso = 90, venceEnDias = 2, diasSinAvance = 5)), 0.1)
    }

    @Test
    fun `el trabajo restante pesa mas cuanto mas cerca esta el deadline`() {
        val lejos = puntaje(tarea(progreso = 0, venceEnDias = 6)) - puntaje(tarea(progreso = 100, venceEnDias = 6))
        val cerca = puntaje(tarea(progreso = 0, venceEnDias = 1)) - puntaje(tarea(progreso = 100, venceEnDias = 1))

        assertTrue(cerca > lejos)
    }

    @Test
    fun `sin deadline el progreso no cambia el puntaje`() {
        assertEquals(puntaje(tarea(progreso = 0, diasSinAvance = 15)), puntaje(tarea(progreso = 90, diasSinAvance = 15)), 0.001)
    }

    @Test
    fun `el puntaje nunca supera cien`() {
        assertEquals(100.0, puntaje(tarea(progreso = 0, venceEnDias = -30, diasSinAvance = 90)), 0.001)
    }
}
