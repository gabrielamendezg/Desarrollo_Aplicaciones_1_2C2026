package ar.edu.uade.piotask.data

import ar.edu.uade.piotask.data.local.Converters
import ar.edu.uade.piotask.data.mapper.toDomain
import ar.edu.uade.piotask.data.mapper.toEntity
import ar.edu.uade.piotask.domain.model.Actividad
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.TipoActividad
import ar.edu.uade.piotask.domain.tarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MappersTest {

    @Test
    fun `una tarea sobrevive al viaje dominio entidad dominio`() {
        val original = tarea(id = 3, progreso = 40, venceEnDias = 5, diasSinAvance = 2, pospuestaDias = 1, ventanaDeadlineDias = 10)
            .copy(completadaEn = Instant.parse("2026-11-02T10:00:00Z"))

        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `una categoria y una actividad sobreviven al viaje`() {
        val categoria = Categoria(2, "Arte", "#DB2777")
        val actividad = Actividad(4, 3, Instant.parse("2026-11-02T10:00:00Z"), TipoActividad.AVANCE, 60)

        assertEquals(categoria, categoria.toEntity().toDomain())
        assertEquals(actividad, actividad.toEntity().toDomain())
    }

    @Test
    fun `los converters guardan fechas como numeros y respetan los nulos`() {
        val c = Converters()
        val instante = Instant.parse("2026-11-02T10:15:30Z")
        val fecha = LocalDate.of(2026, 11, 2)

        assertEquals(instante, c.epochAInstant(c.instantAEpoch(instante)))
        assertEquals(fecha, c.epochDayAFecha(c.fechaAEpochDay(fecha)))
        assertNull(c.instantAEpoch(null))
        assertNull(c.epochDayAFecha(null))
    }
}
