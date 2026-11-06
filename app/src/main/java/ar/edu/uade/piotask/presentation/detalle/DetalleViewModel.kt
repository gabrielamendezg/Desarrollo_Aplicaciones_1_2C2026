package ar.edu.uade.piotask.presentation.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.core.navigation.Rutas
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.EvaluacionAtencion
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import ar.edu.uade.piotask.domain.usecase.CalcularEstadoAtencionUseCase
import ar.edu.uade.piotask.domain.usecase.CompletarTareaUseCase
import ar.edu.uade.piotask.domain.usecase.PosponerTareaUseCase
import ar.edu.uade.piotask.domain.usecase.RegistrarAvanceUseCase
import ar.edu.uade.piotask.presentation.common.Aviso
import ar.edu.uade.piotask.presentation.common.AvisosBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class DetalleUi(
    val tarea: Tarea,
    val categoria: Categoria?,
    val evaluacion: EvaluacionAtencion,
    val diasSinAvance: Long,
    val hoy: LocalDate,
)

@HiltViewModel
class DetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tareaRepository: TareaRepository,
    categoriaRepository: CategoriaRepository,
    preferenciasRepository: PreferenciasRepository,
    private val calcularEstado: CalcularEstadoAtencionUseCase,
    private val registrarAvance: RegistrarAvanceUseCase,
    private val posponerTarea: PosponerTareaUseCase,
    private val completarTarea: CompletarTareaUseCase,
    private val avisos: AvisosBus,
    private val clock: Clock,
) : ViewModel() {

    private val tareaId: Long = checkNotNull(savedStateHandle[Rutas.ARG_TAREA_ID])

    private val canalVolver = Channel<Unit>(Channel.BUFFERED)

    /** Se emite cuando la acción sacó a la tarea de la pantalla (completar o posponer). */
    val volver: Flow<Unit> = canalVolver.receiveAsFlow()

    val uiState: StateFlow<UiState<DetalleUi>> = combine(
        tareaRepository.observarPorId(tareaId),
        categoriaRepository.observarTodas(),
        preferenciasRepository.observarCriterios(),
    ) { tarea, categorias, criterios ->
        val estado: UiState<DetalleUi> = if (tarea == null) {
            UiState.Empty
        } else {
            val hoy = LocalDate.now(clock)
            UiState.Success(
                DetalleUi(
                    tarea = tarea,
                    categoria = categorias.firstOrNull { it.id == tarea.categoriaId },
                    evaluacion = calcularEstado(tarea, criterios, hoy, clock.zone),
                    diasSinAvance = tarea.diasSinAvance(hoy, clock.zone),
                    hoy = hoy,
                ),
            )
        }
        estado
    }
        .catch { emit(UiState.Error(it.message ?: "Error al leer la tarea")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 0),
            initialValue = UiState.Loading,
        )

    fun registrarAvance(progreso: Int) {
        viewModelScope.launch {
            val resultado = registrarAvance.invoke(tareaId, progreso)
            if (resultado is Resource.Error) avisos.enviar(Aviso.Simple(resultado.message))
        }
    }

    fun posponer(hasta: LocalDate) {
        viewModelScope.launch {
            when (val resultado = posponerTarea(tareaId, hasta)) {
                is Resource.Success -> {
                    avisos.enviar(Aviso.Deshacible(R.string.snack_pospuesta, resultado.data))
                    canalVolver.send(Unit)
                }
                is Resource.Error -> avisos.enviar(Aviso.Simple(resultado.message))
            }
        }
    }

    fun completar() {
        viewModelScope.launch {
            when (val resultado = completarTarea(tareaId)) {
                is Resource.Success -> {
                    avisos.enviar(Aviso.Deshacible(R.string.snack_completada, resultado.data))
                    canalVolver.send(Unit)
                }
                is Resource.Error -> avisos.enviar(Aviso.Simple(resultado.message))
            }
        }
    }
}
