package com.zen.pistats.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zen.pistats.dashboard.data.PiStatsRepository
import com.zen.pistats.dashboard.presentation.PiStatsUi
import com.zen.pistats.dashboard.presentation.toUi
import com.zen.pistats.settings.data.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PiStatsWidgetSyncManager(
    private val context: Context,
    private val settingsRepository: AppSettingsRepository,
    private val piStatsRepository: PiStatsRepository,
) {
    fun ensurePeriodicRefresh() {
        val request = PeriodicWorkRequestBuilder<PiStatsWidgetRefreshWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    suspend fun requestImmediateRefresh() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<PiStatsWidgetRefreshWorker>().build(),
        )
    }

    suspend fun showUnconfigured() {
        updateAll(
            isConfigured = false,
            error = null,
            host = "",
            lastUpdated = "",
            cpu = "",
            memory = "",
            disk = "",
            temperature = "",
            uptime = "",
            load = "",
            backup = "",
            services = "",
        )
    }

    suspend fun showError() {
        updateAll(
            isConfigured = true,
            error = "Could not refresh widget",
            host = null,
            lastUpdated = null,
            cpu = null,
            memory = null,
            disk = null,
            temperature = null,
            uptime = null,
            load = null,
            backup = null,
            services = null,
        )
    }

    suspend fun publishStats(stats: PiStatsUi) {
        updateAll(
            isConfigured = true,
            error = null,
            host = stats.host,
            lastUpdated = stats.lastUpdated,
            cpu = stats.cpuPercent,
            memory = stats.memoryUsage,
            disk = stats.diskUsage,
            temperature = stats.temperature,
            uptime = stats.uptime,
            load = stats.loadAverage,
            backup = "${stats.backupSummary} • ${stats.backupDetail}",
            services = stats.services.joinToString("\n") { "${it.name}: ${it.status}" },
        )
    }

    suspend fun refreshFromApi() {
        val settings = settingsRepository.getCurrentSettings()
        if (!settings.isConfigured) {
            showUnconfigured()
            return
        }

        when (val result = piStatsRepository.fetchStats(settings)) {
            is com.zen.pistats.core.domain.Result.Error -> showError()
            is com.zen.pistats.core.domain.Result.Success -> publishStats(result.data.toUi())
        }
    }

    private suspend fun updateAll(
        isConfigured: Boolean,
        error: String?,
        host: String?,
        lastUpdated: String?,
        cpu: String?,
        memory: String?,
        disk: String?,
        temperature: String?,
        uptime: String?,
        load: String?,
        backup: String?,
        services: String?,
    ) {
        withContext(Dispatchers.IO) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PiStatsWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { preferences ->
                    preferences[WidgetPreferences.IS_CONFIGURED] = isConfigured
                    writeString(preferences, WidgetPreferences.ERROR, error)
                    writeString(preferences, WidgetPreferences.HOST, host)
                    writeString(preferences, WidgetPreferences.LAST_UPDATED, lastUpdated)
                    writeString(preferences, WidgetPreferences.CPU, cpu)
                    writeString(preferences, WidgetPreferences.MEMORY, memory)
                    writeString(preferences, WidgetPreferences.DISK, disk)
                    writeString(preferences, WidgetPreferences.TEMPERATURE, temperature)
                    writeString(preferences, WidgetPreferences.UPTIME, uptime)
                    writeString(preferences, WidgetPreferences.LOAD, load)
                    writeString(preferences, WidgetPreferences.BACKUP, backup)
                    writeString(preferences, WidgetPreferences.SERVICES, services)
                }
            }
            PiStatsWidget().updateAll(context)
        }
    }

    private fun writeString(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        key: Preferences.Key<String>,
        value: String?,
    ) {
        if (value == null) {
            preferences.remove(key)
        } else {
            preferences[key] = value
        }
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "pi_stats_widget_periodic_refresh"
        private const val IMMEDIATE_WORK_NAME = "pi_stats_widget_immediate_refresh"
    }
}

class PiStatsWidgetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val settingsRepository = com.zen.pistats.settings.data.DataStoreAppSettingsRepository(applicationContext)
        val repository = com.zen.pistats.dashboard.data.KtorPiStatsRepository(
            com.zen.pistats.core.data.HttpClientFactory.create(),
        )
        val manager = PiStatsWidgetSyncManager(
            context = applicationContext,
            settingsRepository = settingsRepository,
            piStatsRepository = repository,
        )
        manager.refreshFromApi()
        return Result.success()
    }
}

object WidgetPreferences {
    val IS_CONFIGURED = booleanPreferencesKey("is_configured")
    val ERROR = stringPreferencesKey("error")
    val HOST = stringPreferencesKey("host")
    val LAST_UPDATED = stringPreferencesKey("last_updated")
    val CPU = stringPreferencesKey("cpu")
    val MEMORY = stringPreferencesKey("memory")
    val DISK = stringPreferencesKey("disk")
    val TEMPERATURE = stringPreferencesKey("temperature")
    val UPTIME = stringPreferencesKey("uptime")
    val LOAD = stringPreferencesKey("load")
    val BACKUP = stringPreferencesKey("backup")
    val SERVICES = stringPreferencesKey("services")
}
