package com.zen.pistats.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zen.pistats.settings.domain.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pi_stats_settings",
)

class DataStoreAppSettingsRepository(
    private val context: Context,
) : AppSettingsRepository {
    override val settings: Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                baseUrl = preferences[BASE_URL_KEY].orEmpty(),
                authToken = preferences[AUTH_TOKEN_KEY].orEmpty(),
            )
        }

    override suspend fun getCurrentSettings(): AppSettings = settings.first()

    override suspend fun saveSettings(baseUrl: String, authToken: String) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[BASE_URL_KEY] = baseUrl.trim().removeSuffix("/")
            preferences[AUTH_TOKEN_KEY] = authToken.trim()
        }
    }

    private companion object {
        val BASE_URL_KEY = stringPreferencesKey("base_url")
        val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
