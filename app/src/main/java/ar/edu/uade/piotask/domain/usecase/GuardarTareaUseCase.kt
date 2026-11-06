package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import ar.edu.uade.piotask.domain.repository.TareaRepository
import java.time.Clock
import javax.inject.Inject

/**
 * Alta y edición (RF01). Al crear, la última actividad es la fecha de creación; al editar no
 * se toca, porque cambiar el título no cuenta como avance.
 */
class GuardarTareaUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(tarea: Tarea): Resource<Long> = resultado {
        require(tarea.titulo.isNotBlank()) { "El título no puede estar vacío" }
        require(tarea.progreso in 0..100) { "El progreso debe estar entre 0 y 100" }
        require((tarea.ventanaDeadlineDias ?: 1) >= 1) { "La ventana de deadline es de al menos 1 día" }
        require((tarea.ventanaInactividadDias ?: 1) >= 1) { "La ventana de inactividad es de al menos 1 día" }

        val limpia = tarea.copy(titulo = tarea.titulo.trim())
        val ahora = clock.instant()
        val aGuardar = if (limpia.id == 0L) limpia.copy(creadaEn = ahora, ultimaActividad = ahora) else limpia

        val id = tareaRepository.guardar(aGuardar)
        preferenciasRepository.guardarUltimaCategoriaId(aGuardar.categoriaId)
        id
    }
}
