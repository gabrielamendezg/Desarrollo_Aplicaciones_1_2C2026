package ar.edu.uade.piotask.domain.usecase

import ar.edu.uade.piotask.core.model.Resource
import kotlin.coroutines.cancellation.CancellationException

/**
 * Ejecuta [bloque] y lo convierte en [Resource]. Las validaciones con `require` y los errores
 * de la capa de datos terminan como [Resource.Error] con un mensaje que la UI puede mostrar.
 */
internal inline fun <T> resultado(bloque: () -> T): Resource<T> =
    try {
        Resource.Success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Ocurrió un error inesperado", e)
    }
