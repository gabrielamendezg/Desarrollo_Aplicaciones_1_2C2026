package ar.edu.uade.piotask.presentation.common

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.piotask.domain.model.AccionDeshacible
import ar.edu.uade.piotask.domain.usecase.DeshacerAccionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Mensajes que sobreviven al cambio de pantalla: se muestran en el snackbar de la app. */
sealed interface Aviso {
    data class Simple(val texto: String) : Aviso
    data class Deshacible(@StringRes val mensaje: Int, val accion: AccionDeshacible) : Aviso
}

@Singleton
class AvisosBus @Inject constructor() {
    private val canal = Channel<Aviso>(Channel.BUFFERED)
    val avisos: Flow<Aviso> = canal.receiveAsFlow()

    fun enviar(aviso: Aviso) {
        canal.trySend(aviso)
    }
}

@HiltViewModel
class AvisosViewModel @Inject constructor(
    bus: AvisosBus,
    private val deshacerAccion: DeshacerAccionUseCase,
    private val avisosBus: AvisosBus,
) : ViewModel() {
    val avisos: Flow<Aviso> = bus.avisos

    fun deshacer(accion: AccionDeshacible) {
        viewModelScope.launch {
            val resultado = deshacerAccion(accion)
            if (resultado is ar.edu.uade.piotask.core.model.Resource.Error) {
                avisosBus.enviar(Aviso.Simple(resultado.message))
            }
        }
    }
}
