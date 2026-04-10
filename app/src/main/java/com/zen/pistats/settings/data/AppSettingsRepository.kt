package com.zen.pistats.settings.data

import com.zen.pistats.settings.domain.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun getCurrentSettings(): AppSettings

    suspend fun saveSettings(baseUrl: String, authToken: String)
}
