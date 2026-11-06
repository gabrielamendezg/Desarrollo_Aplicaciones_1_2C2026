package ar.edu.uade.piotask.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.navigation.Rutas
import ar.edu.uade.piotask.presentation.ajustes.AjustesScreen
import ar.edu.uade.piotask.presentation.common.Aviso
import ar.edu.uade.piotask.presentation.common.AvisosViewModel
import ar.edu.uade.piotask.presentation.detalle.DetalleScreen
import ar.edu.uade.piotask.presentation.nido.NidoScreen
import ar.edu.uade.piotask.presentation.nueva.NuevaTareaScreen
import ar.edu.uade.piotask.presentation.rama.RamaScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Destino(val ruta: String, val titulo: Int)

private val DESTINOS = listOf(
    Destino(Rutas.RAMA, R.string.nav_rama),
    Destino(Rutas.NIDO, R.string.nav_nido),
    Destino(Rutas.AJUSTES, R.string.nav_ajustes),
)

private const val DURACION_DESHACER_MS = 5_000L

private val ARGUMENTO_TAREA = listOf(navArgument(Rutas.ARG_TAREA_ID) { type = NavType.LongType })

@Composable
fun PioTaskApp(avisosViewModel: AvisosViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val recursos = LocalResources.current
    val textoDeshacer = stringResource(R.string.deshacer)

    // Los avisos viven acá y no en cada pantalla para que sobrevivan a un "volver".
    LaunchedEffect(avisosViewModel) {
        avisosViewModel.avisos.collect { aviso ->
            when (aviso) {
                is Aviso.Simple -> snackbar.showSnackbar(aviso.texto)
                is Aviso.Deshacible -> {
                    // El TPO pide 5 s y el Short de Material dura 4: se cierra a mano.
                    val cierre = launch {
                        delay(DURACION_DESHACER_MS)
                        snackbar.currentSnackbarData?.dismiss()
                    }
                    val respuesta = snackbar.showSnackbar(
                        message = recursos.getString(aviso.mensaje),
                        actionLabel = textoDeshacer,
                        duration = SnackbarDuration.Indefinite,
                    )
                    cierre.cancel()
                    if (respuesta == SnackbarResult.ActionPerformed) avisosViewModel.deshacer(aviso.accion)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (rutaActual == null || DESTINOS.any { it.ruta == rutaActual }) {
                NavigationBar {
                    DESTINOS.forEach { destino ->
                        NavigationBarItem(
                            selected = rutaActual == destino.ruta,
                            onClick = {
                                navController.navigate(destino.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {},
                            label = { Text(stringResource(destino.titulo)) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.RAMA,
            modifier = Modifier.padding(padding),
        ) {
            composable(Rutas.RAMA) {
                RamaScreen(
                    onNuevaTarea = { navController.navigate(Rutas.NUEVA_TAREA) },
                    onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) },
                )
            }
            composable(Rutas.NIDO) {
                NidoScreen(onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) })
            }
            composable(Rutas.AJUSTES) { AjustesScreen() }
            composable(Rutas.NUEVA_TAREA) {
                NuevaTareaScreen(
                    onTerminar = { navController.popBackStack() },
                    onEliminada = { navController.popBackStack() },
                )
            }
            composable(Rutas.DETALLE, arguments = ARGUMENTO_TAREA) {
                DetalleScreen(
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(Rutas.editar(it)) },
                )
            }
            composable(Rutas.EDITAR, arguments = ARGUMENTO_TAREA) {
                NuevaTareaScreen(
                    onTerminar = { navController.popBackStack() },
                    // La tarea ya no existe: se vuelve a la lista, no al Detalle.
                    onEliminada = { navController.popBackStack(Rutas.RAMA, inclusive = false) },
                )
            }
        }
    }
}
