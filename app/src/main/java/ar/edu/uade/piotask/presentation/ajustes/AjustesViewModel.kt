package ar.edu.uade.piotask.presentation.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import ar.edu.uade.piotask.domain.usecase.ActualizarCriteriosUseCase
import ar.edu.uade.piotask.domain.usecase.EliminarCategoriaUseCase
import ar.edu.uade.piotask.domain.usecase.GuardarCategoriaUseCase
import ar.edu.uade.piotask.presentation.common.Aviso
import ar.edu.uade.piotask.presentation.common.AvisosBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriaConConteo(val categoria: Categoria, val tareas: Int)

data class AjustesUi(val categorias: List<CategoriaConConteo>, val criterios: Criterios)

@HiltViewModel
class AjustesViewModel @Inject constructor(
    categoriaRepository: CategoriaRepository,
    tareaRepository: TareaRepository,
    preferenciasRepository: PreferenciasRepository,
    private val guardarCategoria: GuardarCategoriaUseCase,
    private val eliminarCategoria: EliminarCategoriaUseCase,
    private val actualizarCriterios: ActualizarCriteriosUseCase,
    private val avisos: AvisosBus,
) : ViewModel() {

    val uiState: StateFlow<AjustesUi> = combine(
        categoriaRepository.observarTodas(),
        tareaRepository.observarActivas(),
        preferenciasRepository.observarCriterios(),
    ) { categorias, tareas, criterios ->
        AjustesUi(
            categorias = categorias.map { c -> CategoriaConConteo(c, tareas.count { it.categoriaId == c.id }) },
            criterios = criterios,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesUi(emptyList(), Criterios()))

    fun guardar(categoria: Categoria) {
        viewModelScope.launch {
            val resultado = guardarCategoria(categoria)
            if (resultado is Resource.Error) avisos.enviar(Aviso.Simple(resultado.message))
        }
    }

    fun eliminar(categoriaId: Long, reasignarA: Long?) {
        viewModelScope.launch {
            val resultado = eliminarCategoria(categoriaId, reasignarA)
            if (resultado is Resource.Error) avisos.enviar(Aviso.Simple(resultado.message))
        }
    }

    fun cambiarCriterios(nuevos: Criterios) {
        viewModelScope.launch {
            val resultado = actualizarCriterios(nuevos)
            if (resultado is Resource.Error) avisos.enviar(Aviso.Simple(resultado.message))
        }
    }
}
