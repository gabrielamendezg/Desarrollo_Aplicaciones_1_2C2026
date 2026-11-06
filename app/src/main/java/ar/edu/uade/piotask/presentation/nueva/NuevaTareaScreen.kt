package ar.edu.uade.piotask.presentation.nueva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.presentation.common.BotonPrincipal
import ar.edu.uade.piotask.presentation.common.PuntoColor
import ar.edu.uade.piotask.presentation.common.colorDeCategoria
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_FECHA: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.forLanguageTag("es-AR"))

/**
 * Alta y edición de tarea en pocas interacciones (RNF02): escribir el título y guardar, con la
 * última categoría usada ya elegida. Lo escrito sobrevive a una rotación (rememberSaveable).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaTareaScreen(
    onTerminar: () -> Unit,
    onEliminada: () -> Unit,
    viewModel: NuevaTareaViewModel = hiltViewModel(),
) {
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val ultimaCategoriaId by viewModel.ultimaCategoriaId.collectAsStateWithLifecycle()
    val criterios by viewModel.criterios.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var titulo by rememberSaveable { mutableStateOf("") }
    var categoriaElegida by rememberSaveable { mutableStateOf<Long?>(null) }
    var tieneFecha by rememberSaveable { mutableStateOf(false) }
    var diaEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
    var cargada by rememberSaveable { mutableStateOf(false) }
    val original by viewModel.tareaOriginal.collectAsStateWithLifecycle(initialValue = null)

    // Al editar, se completan los campos una sola vez con lo que ya tenía la tarea.
    LaunchedEffect(original) {
        val tarea = original
        if (tarea != null && !cargada) {
            titulo = tarea.titulo
            categoriaElegida = tarea.categoriaId
            tieneFecha = tarea.deadline != null
            diaEpoch = tarea.deadline?.toEpochDay()
            cargada = true
        }
    }

    val categoriaId = categoriaElegida?.takeIf { id -> categorias.any { it.id == id } }
        ?: ultimaCategoriaId?.takeIf { id -> categorias.any { it.id == id } }
        ?: categorias.firstOrNull()?.id
    val fecha = diaEpoch?.let(LocalDate::ofEpochDay)
    val diasParaDespertar = (criterios.ventanaInactividadDias * 4 + 9) / 10

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoNueva.Guardada -> onTerminar()
                EventoNueva.Eliminada -> onEliminada()
                is EventoNueva.Error -> snackbar.showSnackbar(evento.mensaje)
            }
        }
    }

    Scaffold(
        containerColor = PioColores.Fondo,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).statusBarsPadding().navigationBarsPadding().imePadding()) {
            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = onTerminar, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) {
                    Text(
                        stringResource(R.string.cancelar),
                        style = MaterialTheme.typography.labelLarge,
                        color = PioColores.TextoBoton,
                    )
                }
                Text(
                    stringResource(if (viewModel.editando) R.string.editar_titulo else R.string.nueva_titulo),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Campo(stringResource(R.string.nueva_campo_titulo)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        placeholder = { Text(stringResource(R.string.nueva_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PioColores.Tinta,
                            unfocusedBorderColor = PioColores.Borde,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Campo(stringResource(R.string.nueva_campo_categoria)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categorias, key = { it.id }) { categoria ->
                            Opcion(
                                seleccionada = categoria.id == categoriaId,
                                onClick = { categoriaElegida = categoria.id },
                            ) {
                                PuntoColor(colorDeCategoria(categoria.colorHex), 10.dp)
                                Text(categoria.nombre, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Campo(stringResource(R.string.nueva_pregunta_fecha)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Segmento(stringResource(R.string.nueva_sin_fecha), !tieneFecha, Modifier.weight(1f)) {
                            tieneFecha = false
                        }
                        Segmento(stringResource(R.string.nueva_con_fecha), tieneFecha, Modifier.weight(1f)) {
                            tieneFecha = true
                        }
                    }
                }

                if (tieneFecha) {
                    Campo(stringResource(R.string.nueva_fecha_limite)) {
                        Surface(
                            onClick = { mostrarFecha = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, PioColores.Borde),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Row(Modifier.padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    fecha?.format(FORMATO_FECHA) ?: stringResource(R.string.nueva_elegir_fecha),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = PioColores.CirculoPajaro, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (tieneFecha) stringResource(R.string.nueva_aviso_con_fecha)
                        else stringResource(R.string.nueva_aviso_sin_fecha, diasParaDespertar),
                        style = MaterialTheme.typography.bodySmall,
                        color = PioColores.TextoSecundario,
                        modifier = Modifier.padding(14.dp),
                    )
                }

                if (viewModel.editando) {
                    TextButton(onClick = { confirmarEliminar = true }) {
                        Text(
                            stringResource(R.string.accion_eliminar),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = stringResource(R.string.nueva_guardar),
                onClick = { viewModel.guardar(titulo, categoriaId!!, if (tieneFecha) fecha else null) },
                habilitado = titulo.isNotBlank() && categoriaId != null && (!tieneFecha || fecha != null),
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp, top = 8.dp),
            )
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text(stringResource(R.string.eliminar_confirmar_titulo)) },
            text = { Text(stringResource(R.string.eliminar_confirmar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar()
                }) { Text(stringResource(R.string.accion_eliminar), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text(stringResource(R.string.cancelar)) }
            },
        )
    }

    if (mostrarFecha) {
        val estado = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let {
                        diaEpoch = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    mostrarFecha = false
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text(stringResource(R.string.cancelar)) }
            },
        ) { DatePicker(state = estado) }
    }
}

@Composable
private fun Campo(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = PioColores.TextoSecundario)
        contenido()
    }
}

@Composable
private fun Opcion(seleccionada: Boolean, onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(if (seleccionada) 2.dp else 1.dp, if (seleccionada) PioColores.Tinta else PioColores.Borde),
        modifier = Modifier.heightIn(min = 44.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) { contenido() }
    }
}

@Composable
private fun Segmento(texto: String, activo: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (activo) PioColores.Tinta else Color.White,
        contentColor = if (activo) Color.White else PioColores.Tinta,
        border = BorderStroke(1.dp, if (activo) PioColores.Tinta else PioColores.Borde),
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(texto, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}
