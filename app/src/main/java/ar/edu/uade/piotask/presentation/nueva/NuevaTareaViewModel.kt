package ar.edu.uade.piotask.presentation.nueva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.usecase.CrearCategoriasSugeridasUseCase
import ar.edu.uade.piotask.domain.usecase.GuardarTareaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
    data class Error(val mensaje: String) : EventoNueva
}

@HiltViewModel
class NuevaTareaViewModel @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
    preferenciasRepository: PreferenciasRepository,
    private val crearCategoriasSugeridas: CrearCategoriasSugeridasUseCase,
    private val guardarTarea: GuardarTareaUseCase,
) : ViewModel() {

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ultimaCategoriaId: StateFlow<Long?> = preferenciasRepository.observarUltimaCategoriaId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val canalEventos = Channel<EventoNueva>(Channel.BUFFERED)
    val eventos: Flow<EventoNueva> = canalEventos.receiveAsFlow()

    init {
        // Hasta que exista el onboarding, la primera vez se crean Trabajo, Estudio y Arte.
        viewModelScope.launch {
            if (categoriaRepository.observarTodas().first().isEmpty()) crearCategoriasSugeridas()
        }
    }

    fun guardar(titulo: String, categoriaId: Long, deadline: LocalDate?) {
        viewModelScope.launch {
            // Las fechas de creación las pisa GuardarTareaUseCase con el reloj.
            val tarea = Tarea(
                titulo = titulo,
                categoriaId = categoriaId,
                deadline = deadline,
                creadaEn = Instant.EPOCH,
                ultimaActividad = Instant.EPOCH,
            )
            when (val resultado = guardarTarea(tarea)) {
                is Resource.Success -> canalEventos.send(EventoNueva.Guardada)
                is Resource.Error -> canalEventos.send(EventoNueva.Error(resultado.message))
            }
        }
    }
}
