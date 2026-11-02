package ar.edu.uade.piotask.core.model

/**
 * Resultado de una operación de la capa de datos.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}
