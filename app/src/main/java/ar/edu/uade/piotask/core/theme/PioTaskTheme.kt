package ar.edu.uade.piotask.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val Esquema = lightColorScheme(
    primary = PioColores.Tinta,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = PioColores.TextoBoton,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PioColores.CirculoPajaro,
    onPrimaryContainer = PioColores.Tinta,
    secondaryContainer = PioColores.CirculoPajaro,
    onSecondaryContainer = PioColores.Tinta,
    tertiaryContainer = PioColores.DespiertoFondo,
    onTertiaryContainer = PioColores.DespiertoTexto,
    background = PioColores.Fondo,
    onBackground = PioColores.Tinta,
    surface = PioColores.Fondo,
    onSurface = PioColores.Tinta,
    surfaceVariant = PioColores.DormidoFondo,
    onSurfaceVariant = PioColores.TextoSecundario,
    surfaceContainerLow = PioColores.Tarjeta,
    surfaceContainer = PioColores.Tarjeta,
    surfaceContainerHigh = PioColores.Tarjeta,
    surfaceContainerHighest = PioColores.Tarjeta,
    outline = PioColores.BordeBoton,
    outlineVariant = PioColores.Borde,
    error = PioColores.Rojo,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = PioColores.ReclamandoFondo,
    onErrorContainer = PioColores.ReclamandoTexto,
    inverseSurface = PioColores.Tinta,
    inverseOnSurface = androidx.compose.ui.graphics.Color.White,
)

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
)

/** El diseño de Figma es solo claro, así que no se adapta al modo oscuro del sistema. */
@Composable
fun PioTaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = PioTipografia,
        shapes = Formas,
        content = content,
    )
}
