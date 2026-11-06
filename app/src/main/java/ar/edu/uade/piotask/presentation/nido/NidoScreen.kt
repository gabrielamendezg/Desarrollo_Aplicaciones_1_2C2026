package ar.edu.uade.piotask.presentation.nido

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.core.theme.EstiloSeccion
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.presentation.common.Pajaro
import ar.edu.uade.piotask.presentation.common.PuntoColor
import ar.edu.uade.piotask.presentation.common.colorDeCategoria
import ar.edu.uade.piotask.presentation.common.texto

@Composable
fun NidoScreen(onAbrirDetalle: (Long) -> Unit, viewModel: NidoViewModel = hiltViewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        val total = (estado as? UiState.Success)?.data?.grupos?.sumOf { it.tareas.size } ?: 0
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)) {
            Text(stringResource(R.string.seccion_nido), style = EstiloSeccion, color = PioColores.TextoSecundario)
            Spacer(Modifier.height(4.dp))
            Text(
                pluralStringResource(R.plurals.nido_titulo, total, total),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.nido_subtitulo),
                style = MaterialTheme.typography.bodySmall,
                color = PioColores.TextoSecundario,
            )
        }
        Box(Modifier.fillMaxSize()) {
            when (val actual = estado) {
                UiState.Idle, UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                UiState.Empty -> Vacio(stringResource(R.string.nido_vacio))
                is UiState.Error -> Vacio(stringResource(R.string.rama_error_titulo))
                is UiState.Success -> {
                    val ui = actual.data
                    if (ui.grupos.isEmpty()) {
                        Vacio(
                            stringResource(if (ui.hayTareasEnRama) R.string.nido_todo_despierto else R.string.nido_vacio),
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            ui.grupos.forEach { grupo ->
                                item(key = "grupo-${grupo.categoria?.id}") { Encabezado(grupo) }
                                items(grupo.tareas, key = { it.tarea.id }) { evaluada ->
                                    FilaNido(evaluada, grupo, onAbrir = { onAbrirDetalle(evaluada.tarea.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(grupo: GrupoNido) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        if (grupo.categoria != null) {
            PuntoColor(colorDeCategoria(grupo.categoria.colorHex), 10.dp)
            Text(
                "${grupo.categoria.nombre} · ${grupo.tareas.size}",
                style = MaterialTheme.typography.titleSmall,
            )
        } else {
            Text(
                "${stringResource(R.string.sin_categoria)} · ${grupo.tareas.size}",
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun FilaNido(evaluada: TareaEvaluada, grupo: GrupoNido, onAbrir: () -> Unit) {
    Card(
        onClick = onAbrir,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PioColores.Tarjeta),
        border = BorderStroke(1.dp, PioColores.BordeTarjeta),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Pajaro(evaluada.evaluacion.estado, grupo.categoria, 32.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(evaluada.tarea.titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    evaluada.evaluacion.motivo.texto(),
                    style = MaterialTheme.typography.bodySmall,
                    color = PioColores.TextoSecundario,
                )
            }
        }
    }
}

@Composable
private fun Vacio(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
