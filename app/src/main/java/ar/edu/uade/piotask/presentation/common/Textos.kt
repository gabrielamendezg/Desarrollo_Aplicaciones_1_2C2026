package ar.edu.uade.piotask.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.domain.model.EstadoAtencion
import ar.edu.uade.piotask.domain.model.MotivoAtencion
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_FECHA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-AR"))

@Composable
fun MotivoAtencion.texto(): String = when (this) {
    is MotivoAtencion.VenceEn -> when (dias) {
        0L -> stringResource(R.string.motivo_vence_hoy)
        1L -> stringResource(R.string.motivo_vence_manana)
        else -> stringResource(R.string.motivo_vence_en, dias)
    }
    is MotivoAtencion.Vencida -> pluralStringResource(R.plurals.motivo_vencida, dias.toInt(), dias)
    is MotivoAtencion.SinAvance -> pluralStringResource(R.plurals.motivo_sin_avance, dias.toInt(), dias)
    is MotivoAtencion.Pospuesta -> stringResource(R.string.motivo_pospuesta, hasta.format(FORMATO_FECHA))
    MotivoAtencion.Completada -> stringResource(R.string.estado_completada)
}

/** El estado siempre se comunica con texto, nunca solo con color (RNF04). */
@Composable
fun EstadoAtencion.etiqueta(): String = when (this) {
    EstadoAtencion.DORMIDO -> stringResource(R.string.estado_dormido)
    EstadoAtencion.DESPIERTO -> stringResource(R.string.estado_despierto)
    EstadoAtencion.RECLAMANDO -> stringResource(R.string.estado_reclamando)
    EstadoAtencion.COMPLETADA -> stringResource(R.string.estado_completada)
}

fun colorDeCategoria(hex: String): Color =
    runCatching { Color(hex.removePrefix("#").toLong(16) or 0xFF000000) }.getOrDefault(Color.Gray)
