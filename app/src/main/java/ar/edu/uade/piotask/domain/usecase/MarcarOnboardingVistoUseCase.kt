package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import javax.inject.Inject

class MarcarOnboardingVistoUseCase @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
) {
    suspend operator fun invoke(): Resource<Unit> = resultado {
        preferenciasRepository.marcarOnboardingVisto()
    }
}
