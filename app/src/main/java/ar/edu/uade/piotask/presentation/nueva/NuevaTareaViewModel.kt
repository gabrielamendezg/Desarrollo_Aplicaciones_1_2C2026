package ar.edu.uade.piotask.presentation.nueva

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.core.navigation.Rutas
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import ar.edu.uade.piotask.domain.usecase.CrearCategoriasSugeridasUseCase
import ar.edu.uade.piotask.domain.usecase.EliminarTareaUseCase
import ar.edu.uade.piotask.domain.usecase.GuardarTareaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

sealed interface EventoNueva {
    data object Guardada : EventoNueva
    data object Eliminada : EventoNueva
    data class Error(val mensaje: String) : EventoNueva
}

@HiltViewModel
class NuevaTareaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tareaRepository: TareaRepository,
    private val categoriaRepository: CategoriaRepository,
    preferenciasRepository: PreferenciasRepository,
    private val crearCategoriasSugeridas: CrearCategoriasSugeridasUseCase,
    private val guardarTarea: GuardarTareaUseCase,
    private val eliminarTarea: EliminarTareaUseCase,
) : ViewModel() {

    /** Si viene un id, la pantalla edita esa tarea; si no, crea una nueva. */
    private val tareaId: Long? = savedStateHandle[Rutas.ARG_TAREA_ID]
    val editando: Boolean = tareaId != null

    private val original = MutableStateFlow<Tarea?>(null)
    val tareaOriginal: Flow<Tarea?> = original

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ultimaCategoriaId: StateFlow<Long?> = preferenciasRepository.observarUltimaCategoriaId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val canalEventos = Channel<EventoNueva>(Channel.BUFFERED)
    val eventos: Flow<EventoNueva> = canalEventos.receiveAsFlow()

    init {
        tareaId?.let { id -> viewModelScope.launch { original.value = tareaRepository.obtenerPorId(id) } }
        // Hasta que exista el onboarding, la primera vez se crean Trabajo, Estudio y Arte.
        viewModelScope.launch {
            if (categoriaRepository.observarTodas().first().isEmpty()) crearCategoriasSugeridas()
        }
    }

    fun guardar(titulo: String, categoriaId: Long, deadline: LocalDate?) {
        viewModelScope.launch {
            // Las fechas de creación de una tarea nueva las pisa GuardarTareaUseCase con el reloj.
            val base = original.value ?: Tarea(
                titulo = titulo,
                categoriaId = categoriaId,
                creadaEn = Instant.EPOCH,
                ultimaActividad = Instant.EPOCH,
            )
            val tarea = base.copy(titulo = titulo, categoriaId = categoriaId, deadline = deadline)
            when (val resultado = guardarTarea(tarea)) {
                is Resource.Success -> canalEventos.send(EventoNueva.Guardada)
                is Resource.Error -> canalEventos.send(EventoNueva.Error(resultado.message))
            }
        }
    }

    fun eliminar() {
        val id = tareaId ?: return
        viewModelScope.launch {
            when (val resultado = eliminarTarea(id)) {
                is Resource.Success -> canalEventos.send(EventoNueva.Eliminada)
                is Resource.Error -> canalEventos.send(EventoNueva.Error(resultado.message))
            }
        }
    }
}
