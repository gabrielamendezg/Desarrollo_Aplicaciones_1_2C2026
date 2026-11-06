package ar.edu.uade.piotask.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.EstadoAtencion

@Composable
fun EtiquetaEstado(estado: EstadoAtencion, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        EstadoAtencion.RECLAMANDO -> PioColores.ReclamandoFondo to PioColores.ReclamandoTexto
        EstadoAtencion.DESPIERTO -> PioColores.DespiertoFondo to PioColores.DespiertoTexto
        else -> PioColores.DormidoFondo to PioColores.TextoSecundario
    }
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = estado.etiqueta(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun CategoriaConColor(categoria: Categoria, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Spacer(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(colorDeCategoria(categoria.colorHex)),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = categoria.nombre, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun PuntoColor(color: androidx.compose.ui.graphics.Color, tamano: androidx.compose.ui.unit.Dp) {
    Spacer(Modifier.size(tamano).clip(CircleShape).background(color))
}

/** Botón de borde fino del diseño: 44 dp de alto, esquinas de 10 dp y texto marrón. */
@Composable
fun BotonContorno(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rellena: Boolean = false,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PioColores.BordeBoton),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = if (rellena) PioColores.Tarjeta else androidx.compose.ui.graphics.Color.Transparent,
            contentColor = PioColores.TextoBoton,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        modifier = modifier.then(Modifier.heightIn(min = 44.dp)).fillMaxWidth(),
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/** Superficie clara con borde fino, usada para las tarjetas de información del diseño. */
@Composable
fun Panel(modifier: Modifier = Modifier, contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PioColores.Tarjeta)
            .androidxBorde(),
        content = contenido,
    )
}

private fun Modifier.androidxBorde(): Modifier =
    this.then(Modifier.border(1.dp, PioColores.BordeTarjeta, RoundedCornerShape(14.dp)))

/** Botón principal oscuro del diseño (Completar tarea, Guardar tarea). */
@Composable
fun BotonPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = PioColores.Tinta,
            contentColor = androidx.compose.ui.graphics.Color.White,
            disabledContainerColor = PioColores.Borde,
            disabledContentColor = androidx.compose.ui.graphics.Color.White,
        ),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
    }
}
