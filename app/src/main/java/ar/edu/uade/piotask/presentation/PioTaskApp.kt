package ar.edu.uade.piotask.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.navigation.Rutas
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.presentation.ajustes.AjustesScreen
import ar.edu.uade.piotask.presentation.common.Aviso
import ar.edu.uade.piotask.presentation.common.AvisosViewModel
import ar.edu.uade.piotask.presentation.detalle.DetalleScreen
import ar.edu.uade.piotask.presentation.nido.NidoScreen
import ar.edu.uade.piotask.presentation.nueva.NuevaTareaScreen
import ar.edu.uade.piotask.presentation.onboarding.OnboardingScreen
import ar.edu.uade.piotask.presentation.onboarding.OnboardingViewModel
import ar.edu.uade.piotask.presentation.rama.RamaScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Destino(val ruta: String, val titulo: Int, val icono: Int)

private val DESTINOS = listOf(
    Destino(Rutas.RAMA, R.string.nav_rama, R.drawable.ic_nav_rama),
    Destino(Rutas.NIDO, R.string.nav_nido, R.drawable.ic_nav_nido),
    Destino(Rutas.AJUSTES, R.string.nav_ajustes, R.drawable.ic_nav_ajustes),
)

private const val DURACION_DESHACER_MS = 5_000L

private val ARGUMENTO_TAREA = listOf(navArgument(Rutas.ARG_TAREA_ID) { type = NavType.LongType })

@Composable
fun PioTaskApp(
    avisosViewModel: AvisosViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
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

    fun irA(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // La primera vez, antes de mostrar nada, se lanza el onboarding por encima de la Rama.
    val onboardingVisto by onboardingViewModel.visto.collectAsStateWithLifecycle()
    LaunchedEffect(onboardingVisto) {
        if (onboardingVisto == false && navController.currentDestination?.route != Rutas.ONBOARDING) {
            navController.navigate(Rutas.ONBOARDING)
        }
    }
    if (onboardingVisto == null) {
        Box(Modifier.fillMaxSize().background(PioColores.Fondo))
        return
    }

    Scaffold(
        containerColor = PioColores.Fondo,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (rutaActual == null || DESTINOS.any { it.ruta == rutaActual }) {
                BarraInferior(rutaActual, onSeleccionar = ::irA)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.RAMA,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable(Rutas.RAMA) {
                RamaScreen(
                    onNuevaTarea = { navController.navigate(Rutas.NUEVA_TAREA) },
                    onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) },
                    onVerNido = { irA(Rutas.NIDO) },
                )
            }
            composable(Rutas.NIDO) {
                NidoScreen(onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) })
            }
            composable(Rutas.AJUSTES) { AjustesScreen() }
            composable(Rutas.ONBOARDING) {
                OnboardingScreen(
                    onCrearTarea = {
                        navController.popBackStack()
                        navController.navigate(Rutas.NUEVA_TAREA)
                    },
                    onConfigurarCategorias = {
                        navController.popBackStack()
                        irA(Rutas.AJUSTES)
                    },
                )
            }
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

@Composable
private fun BarraInferior(rutaActual: String?, onSeleccionar: (String) -> Unit) {
    Column(Modifier.background(Color.White)) {
        HorizontalDivider(color = PioColores.Borde)
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(80.dp).padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DESTINOS.forEach { destino ->
                val activo = rutaActual == destino.ruta
                val color = if (activo) PioColores.Tinta else PioColores.TextoSecundario
                Column(
                    Modifier
                        .widthIn(min = 64.dp)
                        .heightIn(min = 48.dp)
                        .clickable(onClick = { onSeleccionar(destino.ruta) })
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(destino.icono),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(destino.titulo),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = color,
                    )
                }
            }
        }
    }
}
