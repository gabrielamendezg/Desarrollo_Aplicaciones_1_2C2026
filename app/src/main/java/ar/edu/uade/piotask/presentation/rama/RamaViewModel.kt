package ar.edu.uade.piotask.presentation.rama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.domain.model.TareasEnAtencion
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.usecase.CompletarTareaUseCase
import ar.edu.uade.piotask.domain.usecase.DeshacerAccionUseCase
import ar.edu.uade.piotask.domain.usecase.ObtenerTareasEnAtencionUseCase
import ar.edu.uade.piotask.domain.usecase.RegistrarAvanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TarjetaUi(val evaluada: TareaEvaluada, val categoria: Categoria?)

data class RamaUi(
    val tarjetas: List<TarjetaUi>,
    val ocultas: Int,
    val expandido: Boolean,
    val despiertas: Int,
    val enNido: Int,
    val categorias: List<Categoria>,
    val filtro: Long?,
)

sealed interface EventoRama {
    data class TareaCompletada(val accion: AccionDeshacible) : EventoRama
    data class Error(val mensaje: String) : EventoRama
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RamaViewModel @Inject constructor(
    obtenerTareasEnAtencion: ObtenerTareasEnAtencionUseCase,
    categoriaRepository: CategoriaRepository,
    private val completarTarea: CompletarTareaUseCase,
    private val registrarAvance: RegistrarAvanceUseCase,
    private val deshacerAccion: DeshacerAccionUseCase,
) : ViewModel() {

    private val filtro = MutableStateFlow<Long?>(null)
    private val expandido = MutableStateFlow(false)
    private val reintento = MutableStateFlow(0)
    private val canalEventos = Channel<EventoRama>(Channel.BUFFERED)

    val eventos: Flow<EventoRama> = canalEventos.receiveAsFlow()

    /** Room es la fuente de verdad: cualquier cambio en las tareas recalcula la Rama sola. */
    val uiState: StateFlow<UiState<RamaUi>> = reintento
        .flatMapLatest {
            combine(
                obtenerTareasEnAtencion(),
                categoriaRepository.observarTodas(),
                filtro,
                expandido,
            ) { tareas, categorias, filtroActual, expandidoActual ->
                armar(tareas, categorias, filtroActual, expandidoActual)
            }.catch { emit(UiState.Error(it.message ?: "Error al leer las tareas")) }
        }
        .stateIn(
            scope = viewModelScope,
            // Sin replay tras detenerse: al volver se ve "cargando" y no el último estado, que puede ser viejo.
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 0),
            initialValue = UiState.Loading,
        )

    private fun armar(
        tareas: TareasEnAtencion,
        categorias: List<Categoria>,
        filtroActual: Long?,
        expandidoActual: Boolean,
    ): UiState<RamaUi> {
        if (tareas.visibles.isEmpty() && tareas.masTareas.isEmpty() && tareas.enNido.isEmpty()) {
            return UiState.Empty
        }
        val porId = categorias.associateBy { it.id }
        fun List<TareaEvaluada>.filtradas() = filter { filtroActual == null || it.tarea.categoriaId == filtroActual }
        val visibles = tareas.visibles.filtradas()
        val mas = tareas.masTareas.filtradas()
        val mostradas = if (expandidoActual) visibles + mas else visibles

        return UiState.Success(
            RamaUi(
                tarjetas = mostradas.map { TarjetaUi(it, porId[it.tarea.categoriaId]) },
                ocultas = mas.size,
                expandido = expandidoActual,
                despiertas = tareas.visibles.size + tareas.masTareas.size,
                enNido = tareas.enNido.size,
                categorias = categorias,
                filtro = filtroActual,
            ),
        )
    }

    fun filtrarPor(categoriaId: Long?) {
        filtro.value = categoriaId
    }

    fun alternarExpandido() {
        expandido.value = !expandido.value
    }

    fun reintentar() {
        reintento.value++
    }

    fun completar(tareaId: Long) {
        viewModelScope.launch {
            when (val resultado = completarTarea(tareaId)) {
                is Resource.Success -> canalEventos.send(EventoRama.TareaCompletada(resultado.data))
                is Resource.Error -> canalEventos.send(EventoRama.Error(resultado.message))
            }
        }
    }

    fun registrarAvance(tareaId: Long, progreso: Int) {
        viewModelScope.launch {
            val resultado = registrarAvance.invoke(tareaId, progreso)
            if (resultado is Resource.Error) canalEventos.send(EventoRama.Error(resultado.message))
        }
    }

    fun deshacer(accion: AccionDeshacible) {
        viewModelScope.launch {
            val resultado = deshacerAccion(accion)
            if (resultado is Resource.Error) canalEventos.send(EventoRama.Error(resultado.message))
        }
    }
}
