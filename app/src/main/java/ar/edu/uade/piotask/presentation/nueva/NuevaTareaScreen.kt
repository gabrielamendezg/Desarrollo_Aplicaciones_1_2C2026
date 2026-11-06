package ar.edu.uade.piotask.presentation.nueva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Alta de tarea en pocas interacciones (RNF02): escribir el título y guardar, con la última
 * categoría usada ya elegida. Lo escrito sobrevive a una rotación gracias a rememberSaveable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaTareaScreen(
    onTerminar: () -> Unit,
    viewModel: NuevaTareaViewModel = hiltViewModel(),
) {
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val ultimaCategoriaId by viewModel.ultimaCategoriaId.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var titulo by rememberSaveable { mutableStateOf("") }
    var categoriaElegida by rememberSaveable { mutableStateOf<Long?>(null) }
    var tieneFecha by rememberSaveable { mutableStateOf(false) }
    var diaEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }

    val categoriaId = categoriaElegida?.takeIf { id -> categorias.any { it.id == id } }
        ?: ultimaCategoriaId?.takeIf { id -> categorias.any { it.id == id } }
        ?: categorias.firstOrNull()?.id
    val fecha = diaEpoch?.let(LocalDate::ofEpochDay)

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoNueva.Guardada -> onTerminar()
                is EventoNueva.Error -> snackbar.showSnackbar(evento.mensaje)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.nueva_titulo), style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(stringResource(R.string.nueva_campo_titulo)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.nueva_campo_categoria), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias, key = { it.id }) { categoria ->
                    FilterChip(
                        selected = categoria.id == categoriaId,
                        onClick = { categoriaElegida = categoria.id },
                        label = { Text(categoria.nombre) },
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.nueva_tiene_fecha), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = tieneFecha, onCheckedChange = { tieneFecha = it })
            }
            if (tieneFecha) {
                OutlinedButton(onClick = { mostrarFecha = true }) {
                    Text(
                        fecha?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                            ?: stringResource(R.string.nueva_elegir_fecha),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onTerminar) { Text(stringResource(R.string.cancelar)) }
                Button(
                    onClick = { viewModel.guardar(titulo, categoriaId!!, if (tieneFecha) fecha else null) },
                    enabled = titulo.isNotBlank() && categoriaId != null && (!tieneFecha || fecha != null),
                ) { Text(stringResource(R.string.guardar)) }
            }
        }
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
