package ar.edu.uade.piotask.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.Tarea
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val FORMATO_CORTO: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-AR"))

/** Hoja "Registrar avance": las operaciones críticas no sacan al usuario de su contexto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAvance(tarea: Tarea, onGuardar: (Int) -> Unit, onCerrar: () -> Unit) {
    var valor by rememberSaveable(tarea.id) { mutableFloatStateOf(tarea.progreso.toFloat()) }
    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = PioColores.Fondo) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.hoja_avance_titulo), style = MaterialTheme.typography.titleLarge)
                Text(tarea.titulo, style = MaterialTheme.typography.bodyMedium, color = PioColores.TextoSecundario)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.progreso_valor, tarea.progreso),
                    style = MaterialTheme.typography.titleLarge,
                    color = PioColores.TextoSecundario,
                )
                Text("→", style = MaterialTheme.typography.titleLarge, color = PioColores.TextoSecundario)
                Text(
                    stringResource(R.string.progreso_valor, valor.roundToInt()),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Slider(
                value = valor,
                onValueChange = { valor = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = PioColores.Tinta,
                    activeTrackColor = PioColores.Tinta,
                    inactiveTrackColor = PioColores.CirculoPajaro,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Atajo(stringResource(R.string.atajo_mas, 10)) { valor = (valor + 10).coerceAtMost(100f) }
                Atajo(stringResource(R.string.atajo_mas, 25)) { valor = (valor + 25).coerceAtMost(100f) }
                Atajo(stringResource(R.string.atajo_igual)) { valor = tarea.progreso.toFloat() }
            }
            Aviso(stringResource(R.string.hoja_avance_aviso))
            BotonPrincipal(stringResource(R.string.hoja_avance_guardar), onClick = { onGuardar(valor.roundToInt()) })
        }
    }
}

@Composable
private fun Atajo(texto: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PioColores.Borde),
        modifier = Modifier.heightIn(min = 40.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Aviso(texto: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = PioColores.CirculoPajaro, modifier = Modifier.fillMaxWidth()) {
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = PioColores.TextoSecundario,
            modifier = Modifier.padding(14.dp),
        )
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
    var elegidaEpoch by rememberSaveable { mutableStateOf(hoy.plusWeeks(1).toEpochDay()) }
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    val elegida = LocalDate.ofEpochDay(elegidaEpoch)
    val atajos = listOf(
        R.string.pos_manana to hoy.plusDays(1),
        R.string.pos_3_dias to hoy.plusDays(3),
        R.string.pos_semana to hoy.plusWeeks(1),
    )
    val esAtajo = atajos.any { it.second == elegida }

    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = PioColores.Fondo) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.hoja_posponer_titulo), style = MaterialTheme.typography.titleLarge)
                Text(tarea.titulo, style = MaterialTheme.typography.bodyMedium, color = PioColores.TextoSecundario)
            }
            atajos.forEach { (titulo, fecha) ->
                OpcionFecha(stringResource(titulo), fecha.format(FORMATO_CORTO), fecha == elegida) {
                    elegidaEpoch = fecha.toEpochDay()
                }
            }
            OpcionFecha(
                titulo = stringResource(R.string.pos_fecha),
                detalle = if (esAtajo) "" else elegida.format(FORMATO_CORTO),
                elegida = !esAtajo,
            ) { mostrarFecha = true }
            Aviso(stringResource(R.string.hoja_posponer_aviso))
            BotonPrincipal(
                stringResource(R.string.pos_confirmar, elegida.format(FORMATO_CORTO)),
                onClick = { onElegir(elegida) },
            )
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
                            elegidaEpoch = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
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

@Composable
private fun OpcionFecha(titulo: String, detalle: String, elegida: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(if (elegida) 2.dp else 1.dp, if (elegida) PioColores.Tinta else PioColores.Borde),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            Text(
                if (elegida && detalle.isNotEmpty()) "$detalle ✓" else detalle,
                style = MaterialTheme.typography.bodySmall,
                color = PioColores.TextoSecundario,
            )
        }
    }
}
