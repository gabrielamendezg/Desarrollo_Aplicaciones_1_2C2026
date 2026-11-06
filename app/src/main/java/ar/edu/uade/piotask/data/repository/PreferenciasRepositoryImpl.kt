package ar.edu.uade.piotask.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import ar.edu.uade.piotask.domain.model.Criterios
import ar.edu.uade.piotask.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenciasRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferenciasRepository {

    override fun observarCriterios(): Flow<Criterios> = dataStore.data.map { prefs ->
        val porDefecto = Criterios()
        Criterios(
            ventanaDeadlineDias = prefs[VENTANA_DEADLINE] ?: porDefecto.ventanaDeadlineDias,
            ventanaInactividadDias = prefs[VENTANA_INACTIVIDAD] ?: porDefecto.ventanaInactividadDias,
            limiteVisibles = prefs[LIMITE_VISIBLES] ?: porDefecto.limiteVisibles,
        )
    }

    override suspend fun actualizarCriterios(criterios: Criterios) {
        dataStore.edit { prefs ->
            prefs[VENTANA_DEADLINE] = criterios.ventanaDeadlineDias
            prefs[VENTANA_INACTIVIDAD] = criterios.ventanaInactividadDias
            prefs[LIMITE_VISIBLES] = criterios.limiteVisibles
        }
    }

    override fun observarOnboardingVisto(): Flow<Boolean> =
        dataStore.data.map { it[ONBOARDING_VISTO] ?: false }

    override suspend fun marcarOnboardingVisto() {
        dataStore.edit { it[ONBOARDING_VISTO] = true }
    }

    override fun observarUltimaCategoriaId(): Flow<Long?> =
        dataStore.data.map { it[ULTIMA_CATEGORIA] }

    override suspend fun guardarUltimaCategoriaId(id: Long) {
        dataStore.edit { it[ULTIMA_CATEGORIA] = id }
    }

    private companion object {
        val VENTANA_DEADLINE = intPreferencesKey("ventana_deadline_dias")
        val VENTANA_INACTIVIDAD = intPreferencesKey("ventana_inactividad_dias")
        val LIMITE_VISIBLES = intPreferencesKey("limite_visibles")
        val ONBOARDING_VISTO = booleanPreferencesKey("onboarding_visto")
        val ULTIMA_CATEGORIA = longPreferencesKey("ultima_categoria_id")
    }
}
