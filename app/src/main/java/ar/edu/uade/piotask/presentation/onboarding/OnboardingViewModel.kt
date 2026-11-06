package ar.edu.uade.piotask.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.domain.repository.CategoriaRepository
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.usecase.CrearCategoriasSugeridasUseCase
import ar.edu.uade.piotask.domain.usecase.MarcarOnboardingVistoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    preferenciasRepository: PreferenciasRepository,
    private val categoriaRepository: CategoriaRepository,
    private val crearCategoriasSugeridas: CrearCategoriasSugeridasUseCase,
    private val marcarOnboardingVisto: MarcarOnboardingVistoUseCase,
) : ViewModel() {

    /** `null` mientras se lee la preferencia: así no se parpadea entre la Rama y el onboarding. */
    val visto: StateFlow<Boolean?> = preferenciasRepository.observarOnboardingVisto()
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Crea las tres categorías sugeridas si no hay ninguna, marca el onboarding como visto y sigue. */
    fun terminar(alTerminar: () -> Unit) {
        viewModelScope.launch {
            if (categoriaRepository.observarTodas().first().isEmpty()) crearCategoriasSugeridas()
            marcarOnboardingVisto()
            alTerminar()
        }
    }
}
