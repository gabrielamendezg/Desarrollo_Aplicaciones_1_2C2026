package ar.edu.uade.piotask.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.EstadoAtencion

/** Los tres pájaros del diseño: azul para Trabajo, rojo para Estudio y rosa para Arte. */
enum class ColorPajaro { AZUL, ROJO, ROSA }

private val REFERENCIAS = listOf(
    ColorPajaro.AZUL to PioColores.Azul,
    ColorPajaro.ROJO to PioColores.Rojo,
    ColorPajaro.ROSA to PioColores.Rosa,
)

/**
 * Las categorías las define el usuario, así que el pájaro se elige por cercanía de color
 * con las tres del diseño en lugar de por nombre.
 */
fun colorPajaro(hex: String?): ColorPajaro {
    val color = hex?.let { runCatching { colorDeCategoria(it) }.getOrNull() } ?: return ColorPajaro.AZUL
    return REFERENCIAS.minBy { (_, referencia) -> distancia(color, referencia) }.first
}

private fun distancia(a: Color, b: Color): Float {
    val dr = a.red - b.red
    val dg = a.green - b.green
    val db = a.blue - b.blue
    return dr * dr + dg * dg + db * db
}

@DrawableRes
fun recursoPajaro(estado: EstadoAtencion, color: ColorPajaro): Int = when (estado) {
    EstadoAtencion.DESPIERTO -> when (color) {
        ColorPajaro.AZUL -> R.drawable.pajaro_despierto_azul
        ColorPajaro.ROJO -> R.drawable.pajaro_despierto_rojo
        ColorPajaro.ROSA -> R.drawable.pajaro_despierto_rosa
    }
    EstadoAtencion.RECLAMANDO -> when (color) {
        ColorPajaro.AZUL -> R.drawable.pajaro_reclamando_azul
        ColorPajaro.ROJO -> R.drawable.pajaro_reclamando_rojo
        ColorPajaro.ROSA -> R.drawable.pajaro_reclamando_rosa
    }
    EstadoAtencion.DORMIDO, EstadoAtencion.COMPLETADA -> when (color) {
        ColorPajaro.AZUL -> R.drawable.pajaro_dormido_azul
        ColorPajaro.ROJO -> R.drawable.pajaro_dormido_rojo
        ColorPajaro.ROSA -> R.drawable.pajaro_dormido_rosa
    }
}

/**
 * Pájaro de pixel art. Se dibuja sin suavizado para que los píxeles queden nítidos y lleva
 * descripción para TalkBack, porque el estado nunca se comunica solo con el dibujo (RNF04).
 */
@Composable
fun Pajaro(
    estado: EstadoAtencion,
    categoria: Categoria?,
    tamano: Dp,
    modifier: Modifier = Modifier,
) {
    val descripcion = estado.etiqueta()
    Image(
        bitmap = ImageBitmap.imageResource(recursoPajaro(estado, colorPajaro(categoria?.colorHex))),
        contentDescription = null,
        filterQuality = FilterQuality.None,
        modifier = modifier
            .semantics { contentDescription = descripcion }
            .size(tamano),
    )
}
