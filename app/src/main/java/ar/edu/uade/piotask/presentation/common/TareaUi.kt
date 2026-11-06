package ar.edu.uade.piotask.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.EstadoAtencion

@Composable
fun EtiquetaEstado(estado: EstadoAtencion, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (estado) {
        EstadoAtencion.RECLAMANDO -> colores.errorContainer to colores.onErrorContainer
        EstadoAtencion.DESPIERTO -> colores.tertiaryContainer to colores.onTertiaryContainer
        else -> colores.surfaceVariant to colores.onSurfaceVariant
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
