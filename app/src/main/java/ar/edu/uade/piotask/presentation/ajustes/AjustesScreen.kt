package ar.edu.uade.piotask.presentation.ajustes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.Categoria
import ar.edu.uade.piotask.presentation.common.Panel
import ar.edu.uade.piotask.presentation.common.PuntoColor
import ar.edu.uade.piotask.presentation.common.colorDeCategoria

private val OPCIONES_DEADLINE = listOf(3, 5, 7, 10, 14)
private val OPCIONES_INACTIVIDAD = listOf(14, 21, 30, 45, 60)
private val OPCIONES_VISIBLES = listOf(3, 5, 7)

/** Colores ofrecidos al crear una categoría: los tres del diseño y algunos más. */
private val PALETA = listOf("#2F5FA8", "#B3392F", "#B0407A", "#68B32F", "#DBA717", "#6B4FA0")

@Composable
fun AjustesScreen(viewModel: AjustesViewModel = hiltViewModel()) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Categoria?>(null) }
    var creando by rememberSaveable { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf<CategoriaConConteo?>(null) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(stringResource(R.string.ajustes_titulo), style = MaterialTheme.typography.headlineMedium)

        Seccion(stringResource(R.string.ajustes_categorias)) {
            Panel {
                ui.categorias.forEachIndexed { i, (categoria, tareas) ->
                    if (i > 0) HorizontalDivider(color = PioColores.BordeTarjeta)
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            .clickable { editando = categoria }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PuntoColor(colorDeCategoria(categoria.colorHex), 14.dp)
                        Text(categoria.nombre, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            pluralStringResource(R.plurals.ajustes_tareas, tareas, tareas),
                            style = MaterialTheme.typography.bodySmall,
                            color = PioColores.TextoSecundario,
                        )
                    }
                }
                if (ui.categorias.isNotEmpty()) HorizontalDivider(color = PioColores.BordeTarjeta)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable { creando = true }.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.ajustes_nueva_categoria),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    )
                }
            }
        }

        Seccion(stringResource(R.string.ajustes_cuando_despiertan)) {
            Panel {
                FilaUmbral(
                    titulo = stringResource(R.string.ajustes_ventana_deadline),
                    opciones = OPCIONES_DEADLINE,
                    actual = ui.criterios.ventanaDeadlineDias,
                    formato = { stringResource(R.string.ajustes_dias, it) },
                ) { viewModel.cambiarCriterios(ui.criterios.copy(ventanaDeadlineDias = it)) }
                HorizontalDivider(color = PioColores.BordeTarjeta)
                FilaUmbral(
                    titulo = stringResource(R.string.ajustes_ventana_inactividad),
                    opciones = OPCIONES_INACTIVIDAD,
                    actual = ui.criterios.ventanaInactividadDias,
                    formato = { stringResource(R.string.ajustes_dias, it) },
                ) { viewModel.cambiarCriterios(ui.criterios.copy(ventanaInactividadDias = it)) }
                HorizontalDivider(color = PioColores.BordeTarjeta)
                FilaUmbral(
                    titulo = stringResource(R.string.ajustes_visibles),
                    opciones = OPCIONES_VISIBLES,
                    actual = ui.criterios.limiteVisibles,
                    formato = { it.toString() },
                ) { viewModel.cambiarCriterios(ui.criterios.copy(limiteVisibles = it)) }
            }
            Text(
                stringResource(R.string.ajustes_nota_umbrales),
                style = MaterialTheme.typography.bodySmall,
                color = PioColores.TextoSecundario,
            )
        }
    }

    if (creando) {
        DialogoCategoria(
            inicial = null,
            alGuardar = { viewModel.guardar(it); creando = false },
            alEliminar = null,
            alCerrar = { creando = false },
        )
    }
    editando?.let { categoria ->
        DialogoCategoria(
            inicial = categoria,
            alGuardar = { viewModel.guardar(it); editando = null },
            alEliminar = {
                eliminando = ui.categorias.firstOrNull { c -> c.categoria.id == categoria.id }
                editando = null
            },
            alCerrar = { editando = null },
        )
    }
    eliminando?.let { objetivo ->
        DialogoEliminar(
            objetivo = objetivo,
            otras = ui.categorias.map { it.categoria }.filter { it.id != objetivo.categoria.id },
            alConfirmar = { destino -> viewModel.eliminar(objetivo.categoria.id, destino); eliminando = null },
            alCerrar = { eliminando = null },
        )
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = PioColores.TextoSecundario)
        contenido()
    }
}

@Composable
private fun FilaUmbral(
    titulo: String,
    opciones: List<Int>,
    actual: Int,
    formato: @Composable (Int) -> String,
    alCambiar: (Int) -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(titulo, Modifier.weight(1f).padding(end = 12.dp), style = MaterialTheme.typography.bodyMedium)
        Box {
            Surface(
                onClick = { abierto = true },
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, PioColores.BordeBoton),
                modifier = Modifier.heightIn(min = 40.dp),
            ) {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formato(actual),
                        style = MaterialTheme.typography.labelLarge,
                        color = PioColores.TextoBoton,
                    )
                }
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(formato(opcion)) },
                        onClick = { alCambiar(opcion); abierto = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoCategoria(
    inicial: Categoria?,
    alGuardar: (Categoria) -> Unit,
    alEliminar: (() -> Unit)?,
    alCerrar: () -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf(inicial?.nombre.orEmpty()) }
    var color by rememberSaveable { mutableStateOf(inicial?.colorHex ?: PALETA.first()) }
    AlertDialog(
        onDismissRequest = alCerrar,
        title = {
            Text(stringResource(if (inicial == null) R.string.ajustes_nueva_categoria else R.string.ajustes_editar_categoria))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.ajustes_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.ajustes_color), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (PALETA + listOfNotNull(inicial?.colorHex?.takeIf { it !in PALETA })).forEach { hex ->
                        val elegido = hex.equals(color, ignoreCase = true)
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorDeCategoria(hex))
                                .clickable { color = hex },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (elegido) Spacer(Modifier.size(14.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
                alEliminar?.let {
                    TextButton(onClick = it) {
                        Text(stringResource(R.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank(),
                onClick = { alGuardar((inicial ?: Categoria(nombre = "", colorHex = color)).copy(nombre = nombre, colorHex = color)) },
            ) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text(stringResource(R.string.cancelar)) } },
    )
}

@Composable
private fun DialogoEliminar(
    objetivo: CategoriaConConteo,
    otras: List<Categoria>,
    alConfirmar: (Long?) -> Unit,
    alCerrar: () -> Unit,
) {
    var destino by rememberSaveable { mutableStateOf<Long?>(null) }
    val enUso = objetivo.tareas > 0
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(stringResource(R.string.ajustes_eliminar_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!enUso) {
                    Text(stringResource(R.string.ajustes_eliminar_vacia, objetivo.categoria.nombre))
                } else {
                    Text(stringResource(R.string.ajustes_eliminar_reasignar, objetivo.categoria.nombre))
                    otras.forEach { otra ->
                        val elegida = destino == otra.id
                        Surface(
                            onClick = { destino = otra.id },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(if (elegida) 2.dp else 1.dp, if (elegida) PioColores.Tinta else PioColores.Borde),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                PuntoColor(colorDeCategoria(otra.colorHex), 10.dp)
                                Text(otra.nombre)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enUso || destino != null,
                // Las tareas completadas también cuentan para la regla de borrado: si no hay tareas
                // activas igual se manda un destino, que el caso de uso ignora cuando no hace falta.
                onClick = { alConfirmar(destino ?: otras.firstOrNull()?.id) },
            ) { Text(stringResource(R.string.accion_eliminar), color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text(stringResource(R.string.cancelar)) } },
    )
}
