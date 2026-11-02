package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.domain.HOY
import ar.edu.uade.piotask.domain.ZONA
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.model.EstadoAtencion.COMPLETADA
import ar.edu.uade.piotask.domain.model.EstadoAtencion.DESPIERTO
import ar.edu.uade.piotask.domain.model.EstadoAtencion.DORMIDO
import ar.edu.uade.piotask.domain.model.EstadoAtencion.RECLAMANDO
import ar.edu.uade.piotask.domain.model.EvaluacionAtencion
import ar.edu.uade.piotask.domain.model.MotivoAtencion
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.tarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcularEstadoAtencionUseCaseTest {

    private val calcular = CalcularEstadoAtencionUseCase()
    private val criterios = Criterios()

    private fun evaluar(tarea: Tarea, criterios: Criterios = this.criterios): EvaluacionAtencion =
        calcular(tarea, criterios, HOY, ZONA)

    @Test
    fun `sin deadline ni inactividad duerme`() {
        assertEquals(DORMIDO, evaluar(tarea()).estado)
    }

    @Test
    fun `limites por deadline con la ventana por defecto`() {
        assertEquals(DORMIDO, evaluar(tarea(venceEnDias = 8)).estado)
        assertEquals(DESPIERTO, evaluar(tarea(venceEnDias = 7)).estado)
        assertEquals(DESPIERTO, evaluar(tarea(venceEnDias = 3)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = 2)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = 0)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = -5)).estado)
    }

    @Test
    fun `limites por inactividad con la ventana por defecto`() {
        assertEquals(DORMIDO, evaluar(tarea(diasSinAvance = 11)).estado)
        assertEquals(DESPIERTO, evaluar(tarea(diasSinAvance = 12)).estado)
        assertEquals(DESPIERTO, evaluar(tarea(diasSinAvance = 29)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(diasSinAvance = 30)).estado)
    }

    @Test
    fun `el estado final es el mayor de los dos relojes`() {
        assertEquals(DESPIERTO, evaluar(tarea(venceEnDias = 20, diasSinAvance = 12)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = 5, diasSinAvance = 30)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = 1, diasSinAvance = 12)).estado)
    }

    @Test
    fun `la ventana de la tarea tiene prioridad sobre la global`() {
        assertEquals(DESPIERTO, evaluar(tarea(venceEnDias = 10, ventanaDeadlineDias = 14)).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(diasSinAvance = 5, ventanaInactividadDias = 5)).estado)
    }

    @Test
    fun `una ventana de inactividad de un dia no despierta tareas recien tocadas`() {
        val global = Criterios(ventanaInactividadDias = 1)

        assertEquals(DORMIDO, evaluar(tarea(), global).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(diasSinAvance = 1), global).estado)
    }

    @Test
    fun `una ventana de deadline de un dia reclama recien el dia del vencimiento`() {
        val global = Criterios(ventanaDeadlineDias = 1)

        assertEquals(DESPIERTO, evaluar(tarea(venceEnDias = 1), global).estado)
        assertEquals(RECLAMANDO, evaluar(tarea(venceEnDias = 0), global).estado)
    }

    @Test
    fun `si la fecha del sistema retrocede los dias sin avance no son negativos`() {
        val motivo = evaluar(tarea(diasSinAvance = -10)).motivo

        assertEquals(MotivoAtencion.SinAvance(0), motivo)
    }

    @Test
    fun `una tarea pospuesta descansa aunque lleve dias sin avance`() {
        val evaluacion = evaluar(tarea(diasSinAvance = 40, pospuestaDias = 3))

        assertEquals(DORMIDO, evaluacion.estado)
        assertEquals(MotivoAtencion.Pospuesta(HOY.plusDays(3)), evaluacion.motivo)
        assertTrue(evaluacion.pospuesta)
        assertFalse(evaluacion.requiereAtencion)
    }

    @Test
    fun `al llegar la fecha de posposicion se evalua con los dias reales`() {
        val evaluacion = evaluar(tarea(diasSinAvance = 40, pospuestaDias = 0))

        assertEquals(RECLAMANDO, evaluacion.estado)
        assertFalse(evaluacion.pospuesta)
    }

    @Test
    fun `una tarea pospuesta vuelve antes si reclama por deadline`() {
        assertTrue(evaluar(tarea(venceEnDias = 2, pospuestaDias = 10)).requiereAtencion)
        assertFalse(evaluar(tarea(venceEnDias = 3, pospuestaDias = 10)).requiereAtencion)
    }

    @Test
    fun `una tarea completada sale de la atencion`() {
        val completada = tarea(venceEnDias = -5).copy(completadaEn = HOY.atStartOfDay(ZONA).toInstant())
        val evaluacion = evaluar(completada)

        assertEquals(COMPLETADA, evaluacion.estado)
        assertFalse(evaluacion.requiereAtencion)
    }

    @Test
    fun `el motivo explica el reloj que manda`() {
        assertEquals(MotivoAtencion.VenceEn(2), evaluar(tarea(venceEnDias = 2)).motivo)
        assertEquals(MotivoAtencion.VenceEn(21), evaluar(tarea(venceEnDias = 21)).motivo)
        assertEquals(MotivoAtencion.Vencida(3), evaluar(tarea(venceEnDias = -3)).motivo)
        assertEquals(MotivoAtencion.SinAvance(18), evaluar(tarea(diasSinAvance = 18)).motivo)
        assertEquals(MotivoAtencion.SinAvance(18), evaluar(tarea(venceEnDias = 20, diasSinAvance = 18)).motivo)
    }
}
