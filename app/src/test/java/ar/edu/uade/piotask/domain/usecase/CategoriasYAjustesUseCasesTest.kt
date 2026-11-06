package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.FakeCategoriaRepository
import ar.edu.uade.piotask.domain.FakePreferenciasRepository
import ar.edu.uade.piotask.domain.FakeTareaRepository
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.tarea
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriasYAjustesUseCasesTest {

    private val tareas = FakeTareaRepository()
    private val categorias = FakeCategoriaRepository(
        inicial = listOf(Categoria(1, "Trabajo", "#1D4ED8"), Categoria(2, "Estudio", "#DC2626")),
        tareas = tareas,
    )
    private val prefs = FakePreferenciasRepository()

    private val guardarCategoria = GuardarCategoriaUseCase(categorias)
    private val eliminarCategoria = EliminarCategoriaUseCase(categorias)
    private val actualizarCriterios = ActualizarCriteriosUseCase(prefs)

    @Test
    fun `guardar categoria valida nombre y color`() = runTest {
        assertTrue(guardarCategoria(Categoria(nombre = " ", colorHex = "#FFFFFF")) is Resource.Error)
        assertTrue(guardarCategoria(Categoria(nombre = "Hobbies", colorHex = "rojo")) is Resource.Error)
        assertTrue(guardarCategoria(Categoria(nombre = "Hobbies", colorHex = "#12ab9F")) is Resource.Success)
    }

    @Test
    fun `una categoria sin tareas se elimina directo`() = runTest {
        assertTrue(eliminarCategoria(2) is Resource.Success)
        assertEquals(listOf(1L), categorias.categorias.value.map { it.id })
    }

    @Test
    fun `una categoria con tareas no se elimina hasta reasignarlas`() = runTest {
        tareas.tareas.value = listOf(tarea(id = 1).copy(categoriaId = 2))

        assertTrue(eliminarCategoria(2) is Resource.Error)
        assertEquals(2, categorias.categorias.value.size)

        assertTrue(eliminarCategoria(2, reasignarA = 1) is Resource.Success)
        assertEquals(1L, tareas.tareas.value.single().categoriaId)
        assertEquals(listOf(1L), categorias.categorias.value.map { it.id })
    }

    @Test
    fun `no se puede reasignar a la misma categoria ni a una que no existe`() = runTest {
        tareas.tareas.value = listOf(tarea(id = 1).copy(categoriaId = 2))

        assertTrue(eliminarCategoria(2, reasignarA = 2) is Resource.Error)
        assertTrue(eliminarCategoria(2, reasignarA = 99) is Resource.Error)
        assertEquals(2, categorias.categorias.value.size)
    }

    @Test
    fun `las categorias sugeridas del onboarding son Trabajo, Estudio y Arte`() = runTest {
        val vacio = FakeCategoriaRepository()

        CrearCategoriasSugeridasUseCase(vacio)()

        assertEquals(listOf("Trabajo", "Estudio", "Arte"), vacio.categorias.value.map { it.nombre })
    }

    @Test
    fun `actualizar criterios acepta limite 3, 5 o 7 y ventanas de al menos un dia`() = runTest {
        assertTrue(actualizarCriterios(Criterios(10, 45, 7)) is Resource.Success)
        assertEquals(Criterios(10, 45, 7), prefs.criterios.value)

        assertTrue(actualizarCriterios(Criterios(limiteVisibles = 4)) is Resource.Error)
        assertTrue(actualizarCriterios(Criterios(ventanaDeadlineDias = 0)) is Resource.Error)
        assertTrue(actualizarCriterios(Criterios(ventanaInactividadDias = 0)) is Resource.Error)
        assertEquals(Criterios(10, 45, 7), prefs.criterios.value)
    }

    @Test
    fun `marcar onboarding visto lo persiste`() = runTest {
        MarcarOnboardingVistoUseCase(prefs)()

        assertTrue(prefs.onboardingVisto.value)
    }
}
