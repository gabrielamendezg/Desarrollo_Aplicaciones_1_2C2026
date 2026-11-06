package ar.edu.uade.piotask.presentation.rama

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.presentation.common.CategoriaConColor
import ar.edu.uade.piotask.presentation.common.EtiquetaEstado
import ar.edu.uade.piotask.presentation.common.texto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val DURACION_DESHACER_MS = 5_000L

@Composable
fun RamaScreen(
    onNuevaTarea: () -> Unit,
    viewModel: RamaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val textoCompletada = stringResource(R.string.snack_completada)
    val textoDeshacer = stringResource(R.string.deshacer)
    var tareaConAvance by remember { mutableStateOf<Tarea?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoRama.Error -> snackbar.showSnackbar(evento.mensaje)
                is EventoRama.TareaCompletada -> {
                    // El snackbar de Material dura 4 s como máximo con Short; el TPO pide 5 s.
                    val cierre = launch {
                        delay(DURACION_DESHACER_MS)
                        snackbar.currentSnackbarData?.dismiss()
                    }
                    val respuesta = snackbar.showSnackbar(
                        message = textoCompletada,
                        actionLabel = textoDeshacer,
                        duration = SnackbarDuration.Indefinite,
                    )
                    cierre.cancel()
                    if (respuesta == SnackbarResult.ActionPerformed) viewModel.deshacer(evento.accion)
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaTarea) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                UiState.Idle, UiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                UiState.Empty -> Mensaje(stringResource(R.string.rama_vacio))
                is UiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.rama_error))
                    Button(onClick = viewModel::reintentar) { Text(stringResource(R.string.reintentar)) }
                }
                is UiState.Success -> Contenido(
                    ui = actual.data,
                    onFiltro = viewModel::filtrarPor,
                    onAlternar = viewModel::alternarExpandido,
                    onAvance = { tareaConAvance = it },
                    onCompletar = viewModel::completar,
                )
            }
        }
    }

    tareaConAvance?.let { tarea ->
        HojaAvance(
            tarea = tarea,
            onGuardar = { progreso ->
                viewModel.registrarAvance(tarea.id, progreso)
                tareaConAvance = null
            },
            onCerrar = { tareaConAvance = null },
        )
    }
}

@Composable
private fun Mensaje(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Contenido(
    ui: RamaUi,
    onFiltro: (Long?) -> Unit,
    onAlternar: () -> Unit,
    onAvance: (Tarea) -> Unit,
    onCompletar: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = stringResource(R.string.rama_resumen, ui.despiertas, ui.enNido),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (ui.categorias.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = ui.filtro == null,
                            onClick = { onFiltro(null) },
                            label = { Text(stringResource(R.string.filtro_todas)) },
                        )
                    }
                    items(ui.categorias, key = { it.id }) { categoria ->
                        FilterChip(
                            selected = ui.filtro == categoria.id,
                            onClick = { onFiltro(categoria.id) },
                            label = { Text(categoria.nombre) },
                        )
                    }
                }
            }
        }
        if (ui.tarjetas.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.rama_todo_tranquilo, ui.enNido),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        }
        items(ui.tarjetas, key = { it.evaluada.tarea.id }) { tarjeta ->
            TarjetaTarea(tarjeta, onAvance = onAvance, onCompletar = onCompletar)
        }
        if (ui.ocultas > 0 || ui.expandido) {
            item {
                TextButton(onClick = onAlternar) {
                    Text(
                        if (ui.expandido) stringResource(R.string.rama_ver_menos)
                        else stringResource(R.string.rama_mas_tareas, ui.ocultas),
                    )
                }
            }
        }
        // Deja aire debajo para que el botón "+" no tape la última tarjeta.
        item { Box(Modifier.padding(bottom = 72.dp)) }
    }
}

@Composable
private fun TarjetaTarea(
    tarjeta: TarjetaUi,
    onAvance: (Tarea) -> Unit,
    onCompletar: (Long) -> Unit,
) {
    val (tarea, evaluacion) = tarjeta.evaluada.let { it.tarea to it.evaluacion }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                EtiquetaEstado(evaluacion.estado)
                tarjeta.categoria?.let { CategoriaConColor(it) }
            }
            Text(tarea.titulo, style = MaterialTheme.typography.titleMedium)
            Text(evaluacion.motivo.texto(), style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { tarea.progreso / 100f }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAvance(tarea) }) { Text(stringResource(R.string.accion_avance)) }
                FilledTonalButton(onClick = { onCompletar(tarea.id) }) { Text(stringResource(R.string.accion_completar)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaAvance(tarea: Tarea, onGuardar: (Int) -> Unit, onCerrar: () -> Unit) {
    var valor by rememberSaveable(tarea.id) { mutableFloatStateOf(tarea.progreso.toFloat()) }
    ModalBottomSheet(onDismissRequest = onCerrar) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.hoja_avance_titulo), style = MaterialTheme.typography.titleLarge)
            Text(tarea.titulo, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.progreso_valor, valor.roundToInt()), style = MaterialTheme.typography.headlineSmall)
            Slider(value = valor, onValueChange = { valor = it }, valueRange = 0f..100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { valor = (valor + 10).coerceAtMost(100f) }) {
                    Text(stringResource(R.string.atajo_mas, 10))
                }
                OutlinedButton(onClick = { valor = (valor + 25).coerceAtMost(100f) }) {
                    Text(stringResource(R.string.atajo_mas, 25))
                }
                OutlinedButton(onClick = { valor = tarea.progreso.toFloat() }) {
                    Text(stringResource(R.string.atajo_igual))
                }
            }
            Button(onClick = { onGuardar(valor.roundToInt()) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.hoja_guardar))
            }
        }
    }
}
