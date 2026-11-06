package ar.edu.uade.piotask.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import ar.edu.uade.piotask.core.theme.EstiloSeccion
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.presentation.common.BotonContorno
import ar.edu.uade.piotask.presentation.common.BotonPrincipal
import ar.edu.uade.piotask.presentation.common.Pajaro
import ar.edu.uade.piotask.presentation.common.Panel
import ar.edu.uade.piotask.presentation.common.etiqueta
import java.util.Locale
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
    val formato = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-AR"))
    val creada = tarea.creadaEn.atZone(ZoneId.systemDefault()).toLocalDate().format(formato)
    val completada = tarea.completadaEn != null

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val volver = stringResource(R.string.accion_volver)
            IconButton(onClick = onVolver, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_volver), contentDescription = volver, tint = PioColores.Tinta)
            }
            TextButton(onClick = onEditar, modifier = Modifier.heightIn(min = 44.dp)) {
                Text(
                    stringResource(R.string.accion_editar),
                    style = MaterialTheme.typography.labelLarge,
                    color = PioColores.TextoBoton,
                )
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(148.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
                    contentAlignment = Alignment.Center,
                ) { Pajaro(ui.evaluacion.estado, ui.categoria, 100.dp) }
                Spacer(Modifier.height(14.dp))
                Text(
                    ui.evaluacion.estado.etiqueta(),
                    style = EstiloSeccion,
                    color = PioColores.TextoSecundario,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                ui.categoria?.let {
                    Spacer(Modifier.height(10.dp))
                    CategoriaConColor(it)
                }
            }

            if (completada) {
                Panel { Text(stringResource(R.string.detalle_completada), Modifier.padding(17.dp)) }
            } else {
                Panel {
                    Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.detalle_por_que),
                            style = MaterialTheme.typography.labelMedium,
                            color = PioColores.TextoSecundario,
                        )
                        Text(ui.evaluacion.motivo.texto(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatoPanel(
                    titulo = stringResource(R.string.detalle_ultimo_avance_titulo),
                    valor = if (ui.diasSinAvance == 0L) stringResource(R.string.detalle_hoy)
                    else pluralStringResource(R.plurals.detalle_hace, ui.diasSinAvance.toInt(), ui.diasSinAvance),
                    modifier = Modifier.weight(1f),
                )
                DatoPanel(
                    titulo = stringResource(R.string.detalle_creada_titulo),
                    valor = creada,
                    modifier = Modifier.weight(1f),
                )
            }

            Panel {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            stringResource(R.string.detalle_progreso),
                            style = MaterialTheme.typography.labelMedium,
                            color = PioColores.TextoSecundario,
                        )
                        Text(
                            stringResource(R.string.progreso_valor, tarea.progreso),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { tarea.progreso / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = PioColores.Tinta,
                        trackColor = PioColores.CirculoPajaro,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        if (!completada) {
            Column(
                Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonContorno(
                        stringResource(R.string.hoja_avance_titulo),
                        onClick = onAvance,
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    )
                    BotonContorno(
                        stringResource(R.string.accion_posponer),
                        onClick = onPosponer,
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    )
                }
                BotonPrincipal(stringResource(R.string.accion_completar_tarea), onClick = onCompletar)
            }
        }
    }
}

@Composable
private fun DatoPanel(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Panel(modifier) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = PioColores.TextoSecundario)
            Text(valor, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}
