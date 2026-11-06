package ar.edu.uade.piotask.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.uade.piotask.R
import ar.edu.uade.piotask.core.theme.EstiloSeccion
import ar.edu.uade.piotask.core.theme.PioColores
import ar.edu.uade.piotask.domain.model.EstadoAtencion
import ar.edu.uade.piotask.presentation.common.BotonContorno
import ar.edu.uade.piotask.presentation.common.BotonPrincipal
import ar.edu.uade.piotask.presentation.common.Pajaro

/**
 * Primer uso: explica los tres estados del pájaro en menos de un minuto. Se muestra una sola vez.
 */
@Composable
fun OnboardingScreen(
    onCrearTarea: () -> Unit,
    onConfigurarCategorias: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PioColores.Fondo)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
        ) {
            Spacer(Modifier.height(56.dp))
            Text(stringResource(R.string.onb_bienvenida), style = EstiloSeccion, color = PioColores.TextoSecundario)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.onb_titulo), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(36.dp))
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Estado(EstadoAtencion.DORMIDO, R.string.onb_dormido, R.string.onb_dormido_desc)
                Estado(EstadoAtencion.DESPIERTO, R.string.onb_despierto, R.string.onb_despierto_desc)
                Estado(EstadoAtencion.RECLAMANDO, R.string.onb_reclamando, R.string.onb_reclamando_desc)
            }
        }
        Column(
            Modifier.padding(horizontal = 28.dp).padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BotonPrincipal(
                texto = stringResource(R.string.onb_crear_tarea),
                onClick = { viewModel.terminar(onCrearTarea) },
                modifier = Modifier.height(56.dp),
            )
            BotonContorno(
                texto = stringResource(R.string.onb_configurar),
                onClick = { viewModel.terminar(onConfigurarCategorias) },
                modifier = Modifier.height(48.dp),
            )
        }
    }
}

@Composable
private fun Estado(estado: EstadoAtencion, titulo: Int, descripcion: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(PioColores.CirculoPajaro),
            contentAlignment = Alignment.Center,
        ) { Pajaro(estado, null, 38.dp) }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp))
            Text(
                stringResource(descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = PioColores.TextoSecundario,
            )
        }
    }
}
