package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import javax.inject.Inject

/**
 * Onboarding: crea Trabajo, Estudio y Arte, editables luego en Ajustes.
 */
class CrearCategoriasSugeridasUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    suspend operator fun invoke(): Resource<Unit> = resultado {
        SUGERIDAS.forEach { categoriaRepository.guardar(it) }
    }

    companion object {
        val SUGERIDAS = listOf(
            Categoria(nombre = "Trabajo", colorHex = "#2F5FA8"),
            Categoria(nombre = "Estudio", colorHex = "#B3392F"),
            Categoria(nombre = "Arte", colorHex = "#B0407A"),
        )
    }
}
