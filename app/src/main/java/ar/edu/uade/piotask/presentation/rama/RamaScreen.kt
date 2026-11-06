package ar.edu.uade.piotask.presentation.rama

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.model.UiState
import ar.edu.uade.piotask.core.theme.EstiloSeccion
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.domain.model.EstadoAtencion
import ar.edu.uade.piotask.domain.model.Tarea
import ar.edu.uade.piotask.presentation.common.HojaAvance
import ar.edu.uade.piotask.presentation.common.Pajaro
import ar.edu.uade.piotask.presentation.common.PuntoColor
import ar.edu.uade.piotask.presentation.common.BotonContorno
import ar.edu.uade.piotask.presentation.common.colorDeCategoria
import ar.edu.uade.piotask.presentation.common.texto

private val CIELO = Color(0xFFCADFF0)
private val ESQUELETO = Color(0xFFE5DFD2)
private val ALTO_ILUSTRACION = 266.dp

@Composable
fun RamaScreen(
    onNuevaTarea: () -> Unit,
    onAbrirDetalle: (Long) -> Unit,
    onVerNido: () -> Unit,
    viewModel: RamaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var tareaConAvance by remember { mutableStateOf<Tarea?>(null) }

    Scaffold(
        containerColor = PioColores.FondoRama,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            val descripcion = stringResource(R.string.cd_nueva_tarea)
            FloatingActionButton(
                onClick = onNuevaTarea,
                shape = CircleShape,
                containerColor = PioColores.Tinta,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(2.dp),
                modifier = Modifier.size(60.dp).semantics { contentDescription = descripcion },
            ) {
                Image(painterResource(R.drawable.ic_mas), contentDescription = null)
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                UiState.Idle, UiState.Loading -> Esqueleto()
                UiState.Empty -> VacioSinTareas()
                is UiState.Error -> ErrorLectura(onReintentar = viewModel::reintentar)
                is UiState.Success -> {
                    val ui = actual.data
                    if (ui.despiertas == 0) {
                        TodoTranquilo(ui.enNido, onVerNido)
                    } else {
                        Contenido(
                            ui = ui,
                            onFiltro = viewModel::filtrarPor,
                            onAlternar = viewModel::alternarExpandido,
                            onAvance = { tareaConAvance = it },
                            onCompletar = viewModel::completar,
                            onAbrirDetalle = onAbrirDetalle,
                        )
                    }
                }
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
private fun alturaEstado() = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

/** Cielo, rama y hojas del diseño. La imagen se corre junto con la barra de estado. */
@Composable
private fun Ilustracion() {
    val barra = alturaEstado()
    Box(Modifier.fillMaxWidth().height(ALTO_ILUSTRACION + barra).background(CIELO)) {
        Image(
            painter = painterResource(R.drawable.fondo_rama),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = Modifier.padding(top = barra).fillMaxWidth().height(ALTO_ILUSTRACION),
        )
    }
}

@Composable
private fun Contenido(
    ui: RamaUi,
    onFiltro: (Long?) -> Unit,
    onAlternar: () -> Unit,
    onAvance: (Tarea) -> Unit,
    onCompletar: (Long) -> Unit,
    onAbrirDetalle: (Long) -> Unit,
) {
    val reclamando = ui.tarjetas.count { it.evaluada.evaluacion.estado == EstadoAtencion.RECLAMANDO }
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Box {
                Ilustracion()
                Column(Modifier.padding(top = alturaEstado() + 28.dp).padding(horizontal = 20.dp)) {
                    Text(stringResource(R.string.seccion_rama), style = EstiloSeccion, color = PioColores.TextoSecundario)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = pluralStringResource(R.plurals.rama_titulo, ui.despiertas, ui.despiertas, ui.enNido),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PioColores.Tinta,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Meta(PioColores.Rojo, stringResource(R.string.rama_meta_reclamando, reclamando))
                        Meta(PioColores.Amarillo, stringResource(R.string.rama_meta_despiertos, ui.despiertas - reclamando))
                        Meta(PioColores.Verde, stringResource(R.string.rama_meta_nido, ui.enNido))
                    }
                }
                // Los tres primeros pájaros se posan sobre la rama de la ilustración.
                Row(
                    Modifier.padding(top = alturaEstado() + 159.dp).padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ui.tarjetas.take(3).forEach { Pajaro(it.evaluada.evaluacion.estado, it.categoria, 70.dp) }
                }
            }
        }
        if (ui.categorias.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FiltroChip(selected = ui.filtro == null, onClick = { onFiltro(null) }) {
                            Text(stringResource(R.string.filtro_todas))
                        }
                    }
                    items(ui.categorias, key = { it.id }) { categoria ->
                        FiltroChip(
                            selected = ui.filtro == categoria.id,
                            onClick = { onFiltro(categoria.id) },
                            punto = colorDeCategoria(categoria.colorHex),
                        ) { Text(categoria.nombre) }
                    }
                }
            }
        }
        if (ui.tarjetas.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.rama_categoria_vacia),
                    color = PioColores.TextoSecundario,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }
        items(ui.tarjetas, key = { it.evaluada.tarea.id }) { tarjeta ->
            TarjetaTarea(
                tarjeta = tarjeta,
                onAvance = onAvance,
                onCompletar = onCompletar,
                onAbrir = { onAbrirDetalle(tarjeta.evaluada.tarea.id) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        if (ui.ocultas > 0 || ui.expandido) {
            item {
                Text(
                    text = if (ui.expandido) stringResource(R.string.rama_ver_menos)
                    else stringResource(R.string.rama_mas_tareas, ui.ocultas),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PioColores.TextoSecundario,
                    modifier = Modifier.clickable(onClick = onAlternar).padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
        item {
            Text(
                text = pluralStringResource(R.plurals.rama_pie_nido, ui.enNido, ui.enNido),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = PioColores.TextoSecundario,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun Meta(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PuntoColor(color, 8.dp)
        Text(texto, style = MaterialTheme.typography.labelMedium, color = PioColores.TextoSecundario)
    }
}

@Composable
private fun FiltroChip(
    selected: Boolean,
    onClick: () -> Unit,
    punto: Color? = null,
    etiqueta: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = etiqueta,
        leadingIcon = punto?.let { { PuntoColor(it, 8.dp) } },
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = PioColores.Tinta,
            selectedContainerColor = PioColores.Tinta,
            selectedLabelColor = Color.White,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = PioColores.Borde,
            selectedBorderColor = PioColores.Tinta,
        ),
        modifier = Modifier.height(36.dp),
    )
}

@Composable
private fun TarjetaTarea(
    tarjeta: TarjetaUi,
    onAvance: (Tarea) -> Unit,
    onCompletar: (Long) -> Unit,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tarea = tarjeta.evaluada.tarea
    val evaluacion = tarjeta.evaluada.evaluacion
    val motivo = evaluacion.motivo.texto()
    val meta = tarjeta.categoria?.let { stringResource(R.string.tarjeta_meta, it.nombre, motivo, tarea.progreso) }
        ?: stringResource(R.string.tarjeta_meta_sin_categoria, motivo, tarea.progreso)

    Card(
        onClick = onAbrir,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PioColores.Tarjeta),
        border = BorderStroke(2.dp, PioColores.BordeTarjeta),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CirculoPajaro(evaluacion.estado, tarjeta.categoria)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tarea.titulo, style = MaterialTheme.typography.titleMedium, color = PioColores.Tinta)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tarjeta.categoria?.let {
                            Box(Modifier.padding(top = 5.dp)) { PuntoColor(colorDeCategoria(it.colorHex), 8.dp) }
                        }
                        Text(meta, style = MaterialTheme.typography.bodySmall, color = PioColores.TextoSecundario)
                    }
                }
            }
            Column(Modifier.width(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BotonContorno(stringResource(R.string.accion_avance), onClick = { onAvance(tarea) }, rellena = true)
                BotonContorno(stringResource(R.string.accion_completar), onClick = { onCompletar(tarea.id) })
            }
        }
    }
}

@Composable
private fun CirculoPajaro(estado: EstadoAtencion, categoria: Categoria?) {
    Box(
        Modifier.size(56.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
        contentAlignment = Alignment.Center,
    ) { Pajaro(estado, categoria, 40.dp) }
}

@Composable
private fun TodoTranquilo(enNido: Int, onVerNido: () -> Unit) {
    Column(Modifier.fillMaxSize().background(PioColores.Fondo)) {
        Column(Modifier.padding(top = alturaEstado() + 28.dp).padding(horizontal = 20.dp)) {
            Text(stringResource(R.string.seccion_rama), style = EstiloSeccion, color = PioColores.TextoSecundario)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.rama_tranquilo_titulo), style = MaterialTheme.typography.headlineMedium)
        }
        Column(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(160.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
                contentAlignment = Alignment.Center,
            ) { Pajaro(EstadoAtencion.DORMIDO, null, 104.dp) }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.rama_tranquilo_sub),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.rama_tranquilo_cuerpo, enNido),
                style = MaterialTheme.typography.bodyMedium,
                color = PioColores.TextoSecundario,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BotonContorno(stringResource(R.string.rama_ver_nido), onClick = onVerNido, modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun VacioSinTareas() {
    Column(
        Modifier.fillMaxSize().background(PioColores.Fondo).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(160.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
            contentAlignment = Alignment.Center,
        ) { Pajaro(EstadoAtencion.DORMIDO, null, 104.dp) }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.rama_vacio),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorLectura(onReintentar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(PioColores.Fondo).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(120.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
            contentAlignment = Alignment.Center,
        ) { Pajaro(EstadoAtencion.RECLAMANDO, null, 72.dp) }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.rama_error_titulo), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.rama_error_detalle),
            style = MaterialTheme.typography.bodyMedium,
            color = PioColores.TextoSecundario,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        BotonContorno(stringResource(R.string.reintentar), onClick = onReintentar, modifier = Modifier.height(52.dp))
    }
}

/** Carga breve de la Rama: bloques grises con la forma del contenido (frame 08). */
@Composable
private fun Esqueleto() {
    Column(Modifier.fillMaxSize().background(PioColores.Fondo).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(alturaEstado() + 28.dp))
        Text(stringResource(R.string.seccion_rama), style = EstiloSeccion, color = PioColores.TextoSecundario)
        Spacer(Modifier.height(10.dp))
        Bloque(230.dp, 28.dp)
        Spacer(Modifier.height(10.dp))
        Bloque(170.dp, 14.dp)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bloque(64.dp, 36.dp)
            Bloque(88.dp, 36.dp)
            Bloque(80.dp, 36.dp)
        }
        Spacer(Modifier.height(20.dp))
        repeat(3) {
            Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(ESQUELETO))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Bloque(80.dp, 10.dp)
                    Bloque(200.dp, 16.dp)
                    Bloque(140.dp, 12.dp)
                }
            }
        }
    }
}

@Composable
private fun Bloque(ancho: androidx.compose.ui.unit.Dp, alto: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(ancho, alto).clip(RoundedCornerShape(8.dp)).background(ESQUELETO))
}
