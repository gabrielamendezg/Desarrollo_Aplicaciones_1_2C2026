package ar.edu.uade.piotask.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.domain.model.Tarea
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

/** Hoja "Registrar avance": las operaciones críticas no sacan al usuario de su contexto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAvance(tarea: Tarea, onGuardar: (Int) -> Unit, onCerrar: () -> Unit) {
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

/** Hoja "Posponer hasta": mañana, en 3 días, en 1 semana o una fecha exacta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaPosponer(
    tarea: Tarea,
    hoy: LocalDate,
    onElegir: (LocalDate) -> Unit,
    onCerrar: () -> Unit,
) {
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onCerrar) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.hoja_posponer_titulo), style = MaterialTheme.typography.titleLarge)
            Text(tarea.titulo, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.hoja_posponer_aviso), style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { onElegir(hoy.plusDays(1)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pos_manana))
            }
            OutlinedButton(onClick = { onElegir(hoy.plusDays(3)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pos_3_dias))
            }
            OutlinedButton(onClick = { onElegir(hoy.plusWeeks(1)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pos_semana))
            }
            Button(onClick = { mostrarFecha = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pos_fecha))
            }
        }
    }

    if (mostrarFecha) {
        val posteriores = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(hoy)
        }
        val estado = rememberDatePickerState(selectableDates = posteriores)
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(
                    enabled = estado.selectedDateMillis != null,
                    onClick = {
                        estado.selectedDateMillis?.let {
                            onElegir(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        mostrarFecha = false
                    },
                ) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text(stringResource(R.string.cancelar)) }
            },
        ) { DatePicker(state = estado) }
    }
}
