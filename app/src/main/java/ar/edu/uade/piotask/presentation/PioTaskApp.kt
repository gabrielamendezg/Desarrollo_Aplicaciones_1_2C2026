package ar.edu.uade.piotask.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.navigation.Rutas
import ar.edu.uade.piotask.presentation.ajustes.AjustesScreen
import ar.edu.uade.piotask.presentation.nido.NidoScreen
import ar.edu.uade.piotask.presentation.nueva.NuevaTareaScreen
import ar.edu.uade.piotask.presentation.rama.RamaScreen

private data class Destino(val ruta: String, val titulo: Int)

private val DESTINOS = listOf(
    Destino(Rutas.RAMA, R.string.nav_rama),
    Destino(Rutas.NIDO, R.string.nav_nido),
    Destino(Rutas.AJUSTES, R.string.nav_ajustes),
)

@Composable
fun PioTaskApp() {
    val navController = rememberNavController()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (rutaActual != Rutas.NUEVA_TAREA) {
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
                RamaScreen(onNuevaTarea = { navController.navigate(Rutas.NUEVA_TAREA) })
            }
            composable(Rutas.NIDO) { NidoScreen() }
            composable(Rutas.AJUSTES) { AjustesScreen() }
            composable(Rutas.NUEVA_TAREA) {
                NuevaTareaScreen(onTerminar = { navController.popBackStack() })
            }
        }
    }
}
