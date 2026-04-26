package com.zen.pistats.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zen.pistats.core.domain.Result
import com.zen.pistats.core.presentation.toUiText
import com.zen.pistats.dashboard.data.PiStatsRepository
import com.zen.pistats.settings.data.AppSettingsRepository
import com.zen.pistats.widget.PiStatsWidgetSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DashboardViewModel(
    private val piStatsRepository: PiStatsRepository,
    private val settingsRepository: AppSettingsRepository,
    private val widgetSyncManager: PiStatsWidgetSyncManager,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val refreshMutex = Mutex()
    private var pollingJob: Job? = null

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.OnManualRefreshClick -> refresh(manual = true)
            DashboardAction.OnScreenStarted -> startPolling()
            DashboardAction.OnScreenStopped -> stopPolling()
            DashboardAction.OnWakePcClick -> wakePc()
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (true) {
                refresh(manual = false)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun refresh(manual: Boolean) {
        viewModelScope.launch {
            refreshMutex.withLock {
                val settings = settingsRepository.getCurrentSettings()

                if (!settings.isConfigured) {
                    widgetSyncManager.showUnconfigured()
                    _state.update {
                        it.copy(
                            isConfigured = false,
                            isLoading = false,
                            isRefreshing = false,
                            hostLabel = settings.baseUrl,
                            stats = null,
                            error = null,
                            wakePcStatus = WakePcStatus.Idle,
                            wakePcError = null,
                        )
                    }
                    return@withLock
                }

                val hasStats = _state.value.stats != null
                _state.update {
                    it.copy(
                        isConfigured = true,
                        isLoading = !hasStats && !manual,
                        isRefreshing = hasStats || manual,
                        hostLabel = settings.baseUrl,
                        error = null,
                    )
                }

                when (val result = piStatsRepository.fetchStats(settings)) {
                    is Result.Error -> {
                        widgetSyncManager.showError()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.error.toUiText(),
                            )
                        }
                    }

                    is Result.Success -> {
                        widgetSyncManager.publishStats(result.data.toUi())
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                stats = result.data.toUi(),
                                error = null,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun wakePc() {
        viewModelScope.launch {
            val settings = settingsRepository.getCurrentSettings()

            if (!settings.isConfigured) {
                _state.update {
                    it.copy(
                        wakePcStatus = WakePcStatus.Failed,
                        wakePcError = com.zen.pistats.core.presentation.UiText.StringResource(
                            com.zen.pistats.R.string.config_missing_title,
                        ),
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    wakePcStatus = WakePcStatus.Waking,
                    wakePcError = null,
                )
            }

            when (val result = piStatsRepository.wakePc(settings)) {
                is Result.Error -> _state.update {
                    it.copy(
                        wakePcStatus = WakePcStatus.Failed,
                        wakePcError = result.error.toUiText(),
                    )
                }

                is Result.Success -> _state.update {
                    it.copy(
                        wakePcStatus = WakePcStatus.Success,
                        wakePcError = null,
                    )
                }
            }
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
    }
}
