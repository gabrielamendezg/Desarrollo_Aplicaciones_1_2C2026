package ar.edu.uade.piotask.presentation.nido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.usecase.ObtenerTareasEnAtencionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Una categoría con sus tareas dormidas o pospuestas; `categoria` es null si quedó huérfana. */
data class GrupoNido(val categoria: Categoria?, val tareas: List<TareaEvaluada>)

data class NidoUi(val grupos: List<GrupoNido>, val hayTareasEnRama: Boolean)

@HiltViewModel
class NidoViewModel @Inject constructor(
    obtenerTareasEnAtencion: ObtenerTareasEnAtencionUseCase,
    categoriaRepository: CategoriaRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState<NidoUi>> = combine(
        obtenerTareasEnAtencion(),
        categoriaRepository.observarTodas(),
    ) { tareas, categorias ->
        val porId = categorias.associateBy { it.id }
        val grupos = tareas.enNido
            .groupBy { it.tarea.categoriaId }
            .map { (categoriaId, lista) -> GrupoNido(porId[categoriaId], lista) }
            .sortedBy { it.categoria?.id ?: Long.MAX_VALUE }
        UiState.Success(NidoUi(grupos, hayTareasEnRama = tareas.visibles.isNotEmpty() || tareas.masTareas.isNotEmpty()))
    }
        .map<UiState.Success<NidoUi>, UiState<NidoUi>> { it }
        .catch { emit(UiState.Error(it.message ?: "Error al leer las tareas")) }
        .stateIn(
            scope = viewModelScope,
            // Sin replay tras detenerse: al volver se ve "cargando" y no el último estado, que puede ser viejo.
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 0),
            initialValue = UiState.Loading,
        )
}
