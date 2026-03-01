package pl.coopsoft.szambelan.core.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.coopsoft.szambelan.domain.model.MeterData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Persistence @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val CURRENT_GARDEN = floatPreferencesKey("current_garden")
        val CURRENT_MAIN = floatPreferencesKey("current_main")
        val EMPTY_ACTIONS = stringPreferencesKey("empty_actions")
        val OLD_GARDEN = floatPreferencesKey("old_garden")
        val OLD_MAIN = floatPreferencesKey("old_main")
        val THEME_MODE = intPreferencesKey("theme_mode")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val themeModeFlow: Flow<Int> = dataStore.data.map { it[Keys.THEME_MODE] ?: 0 }
    val userEmailFlow: Flow<String> = dataStore.data.map { it[Keys.USER_EMAIL] ?: "" }

    suspend fun setThemeMode(mode: Int) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { it[Keys.USER_EMAIL] = email }
    }

    suspend fun getMeterData(): MeterData {
        val prefs = dataStore.data.first()
        return MeterData(
            currentGarden = prefs[Keys.CURRENT_GARDEN]?.toDouble() ?: 0.0,
            currentMain = prefs[Keys.CURRENT_MAIN]?.toDouble() ?: 0.0,
            oldGarden = prefs[Keys.OLD_GARDEN]?.toDouble() ?: 0.0,
            oldMain = prefs[Keys.OLD_MAIN]?.toDouble() ?: 0.0,
            emptyActions = prefs[Keys.EMPTY_ACTIONS] ?: ""
        )
    }

    suspend fun saveMeterData(
        currentGarden: Double,
        currentMain: Double,
        oldGarden: Double,
        oldMain: Double
    ) {
        dataStore.edit {
            it[Keys.CURRENT_GARDEN] = currentGarden.toFloat()
            it[Keys.CURRENT_MAIN] = currentMain.toFloat()
            it[Keys.OLD_GARDEN] = oldGarden.toFloat()
            it[Keys.OLD_MAIN] = oldMain.toFloat()
        }
    }

    suspend fun saveEmptyActions(actions: String) {
        dataStore.edit { it[Keys.EMPTY_ACTIONS] = actions }
    }
}
