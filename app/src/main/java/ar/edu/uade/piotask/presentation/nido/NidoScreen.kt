package ar.edu.uade.piotask.presentation.nido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.domain.model.TareaEvaluada
import ar.edu.uade.piotask.presentation.common.CategoriaConColor
import ar.edu.uade.piotask.presentation.common.EtiquetaEstado
import ar.edu.uade.piotask.presentation.common.texto

@Composable
fun NidoScreen(onAbrirDetalle: (Long) -> Unit, viewModel: NidoViewModel = hiltViewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        when (val actual = estado) {
            UiState.Idle, UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            UiState.Empty -> Vacio(stringResource(R.string.nido_vacio))
            is UiState.Error -> Vacio(stringResource(R.string.rama_error))
            is UiState.Success -> {
                val ui = actual.data
                if (ui.grupos.isEmpty()) {
                    Vacio(
                        stringResource(if (ui.hayTareasEnRama) R.string.nido_todo_despierto else R.string.nido_vacio),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ui.grupos.forEach { grupo ->
                            item(key = "grupo-${grupo.categoria?.id}") {
                                Encabezado(grupo)
                            }
                            items(grupo.tareas, key = { it.tarea.id }) { evaluada ->
                                FilaNido(evaluada.tarea.titulo, evaluada, onAbrir = { onAbrirDetalle(evaluada.tarea.id) })
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
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        if (grupo.categoria != null) {
            CategoriaConColor(grupo.categoria)
        } else {
            Text(stringResource(R.string.sin_categoria), style = MaterialTheme.typography.labelMedium)
        }
        Text(grupo.tareas.size.toString(), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FilaNido(titulo: String, evaluada: TareaEvaluada, onAbrir: () -> Unit) {
    Card(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            EtiquetaEstado(evaluada.evaluacion.estado)
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(evaluada.evaluacion.motivo.texto(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Vacio(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
