package ar.edu.uade.piotask.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ar.edu.uade.piotask.presentation.common.CategoriaConColor
import ar.edu.uade.piotask.presentation.common.EtiquetaEstado
import ar.edu.uade.piotask.presentation.common.HojaAvance
import ar.edu.uade.piotask.presentation.common.HojaPosponer
import ar.edu.uade.piotask.presentation.common.texto
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetalleScreen(
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: DetalleViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var hojaAvance by remember { mutableStateOf(false) }
    var hojaPosponer by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) { viewModel.volver.collect { onVolver() } }

    Box(Modifier.fillMaxSize()) {
        when (val actual = estado) {
            UiState.Idle, UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            UiState.Empty -> Column(
                Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.detalle_no_existe), textAlign = TextAlign.Center)
                Button(onClick = onVolver) { Text(stringResource(R.string.accion_volver)) }
            }
            is UiState.Error -> Column(
                Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.rama_error), textAlign = TextAlign.Center)
                Button(onClick = onVolver) { Text(stringResource(R.string.accion_volver)) }
            }
            is UiState.Success -> {
                val ui = actual.data
                Contenido(
                    ui = ui,
                    onVolver = onVolver,
                    onAvance = { hojaAvance = true },
                    onPosponer = { hojaPosponer = true },
                    onCompletar = viewModel::completar,
                    onEditar = { onEditar(ui.tarea.id) },
                )
                if (hojaAvance) {
                    HojaAvance(
                        tarea = ui.tarea,
                        onGuardar = {
                            viewModel.registrarAvance(it)
                            hojaAvance = false
                        },
                        onCerrar = { hojaAvance = false },
                    )
                }
                if (hojaPosponer) {
                    HojaPosponer(
                        tarea = ui.tarea,
                        hoy = ui.hoy,
                        onElegir = {
                            viewModel.posponer(it)
                            hojaPosponer = false
                        },
                        onCerrar = { hojaPosponer = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun Contenido(
    ui: DetalleUi,
    onVolver: () -> Unit,
    onAvance: () -> Unit,
    onPosponer: () -> Unit,
    onCompletar: () -> Unit,
    onEditar: () -> Unit,
) {
    val tarea = ui.tarea
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val creada = tarea.creadaEn.atZone(ZoneId.systemDefault()).toLocalDate().format(formato)
    val completada = tarea.completadaEn != null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onVolver) { Text(stringResource(R.string.accion_volver)) }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EtiquetaEstado(ui.evaluacion.estado)
            ui.categoria?.let { CategoriaConColor(it) }
        }

        Text(tarea.titulo, style = MaterialTheme.typography.headlineMedium)

        if (completada) {
            Text(stringResource(R.string.detalle_completada), style = MaterialTheme.typography.bodyLarge)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.detalle_por_que), style = MaterialTheme.typography.labelLarge)
                Text(ui.evaluacion.motivo.texto(), style = MaterialTheme.typography.bodyLarge)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(progress = { tarea.progreso / 100f }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.progreso_valor, tarea.progreso), style = MaterialTheme.typography.bodyMedium)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (ui.diasSinAvance == 0L) {
                    stringResource(R.string.detalle_ultimo_avance_hoy)
                } else {
                    pluralStringResource(R.plurals.detalle_ultimo_avance, ui.diasSinAvance.toInt(), ui.diasSinAvance)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(stringResource(R.string.detalle_creada, creada), style = MaterialTheme.typography.bodyMedium)
        }

        if (!completada) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAvance, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.hoja_avance_titulo))
                }
                FilledTonalButton(onClick = onPosponer, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.accion_posponer))
                }
                FilledTonalButton(onClick = onCompletar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.accion_completar))
                }
                OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.accion_editar))
                }
            }
        }
    }
}
