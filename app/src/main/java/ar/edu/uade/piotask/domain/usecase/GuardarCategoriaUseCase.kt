package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import javax.inject.Inject

class GuardarCategoriaUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    suspend operator fun invoke(categoria: Categoria): Resource<Long> = resultado {
        require(categoria.nombre.isNotBlank()) { "El nombre no puede estar vacío" }
        require(COLOR.matches(categoria.colorHex)) { "El color tiene que ser un código como #1A73E8" }
        categoriaRepository.guardar(categoria.copy(nombre = categoria.nombre.trim()))
    }

    private companion object {
        val COLOR = Regex("^#[0-9A-Fa-f]{6}$")
    }
}
