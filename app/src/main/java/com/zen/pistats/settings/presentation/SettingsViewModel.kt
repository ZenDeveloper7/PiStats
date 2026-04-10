package com.zen.pistats.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zen.pistats.R
import com.zen.pistats.core.presentation.UiText
import com.zen.pistats.settings.data.AppSettingsRepository
import com.zen.pistats.widget.PiStatsWidgetSyncManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: AppSettingsRepository,
    private val widgetSyncManager: PiStatsWidgetSyncManager,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _state.update {
                it.copy(
                    baseUrl = settings.baseUrl,
                    authToken = settings.authToken,
                )
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnAuthTokenChanged -> _state.update {
                it.copy(authToken = action.value, error = null)
            }

            is SettingsAction.OnBaseUrlChanged -> _state.update {
                it.copy(baseUrl = action.value, error = null)
            }

            SettingsAction.OnSaveClick -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val normalizedUrl = current.baseUrl.trim().removeSuffix("/")
        val normalizedToken = current.authToken.trim()

        val error = when {
            normalizedUrl.isBlank() -> UiText.StringResource(R.string.error_missing_base_url)
            normalizedToken.isBlank() -> UiText.StringResource(R.string.error_missing_auth_token)
            !normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://") ->
                UiText.StringResource(R.string.error_invalid_base_url)

            else -> null
        }

        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            settingsRepository.saveSettings(
                baseUrl = normalizedUrl,
                authToken = normalizedToken,
            )
            widgetSyncManager.requestImmediateRefresh()
            _state.update { it.copy(isSaving = false) }
            _events.send(SettingsEvent.Saved)
        }
    }
}
