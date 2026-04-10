package com.zen.pistats.settings.presentation

import androidx.compose.runtime.Stable
import com.zen.pistats.core.presentation.UiText

sealed interface SettingsAction {
    data class OnBaseUrlChanged(val value: String) : SettingsAction
    data class OnAuthTokenChanged(val value: String) : SettingsAction
    data object OnSaveClick : SettingsAction
}

sealed interface SettingsEvent {
    data object Saved : SettingsEvent
}

@Stable
data class SettingsState(
    val baseUrl: String = "",
    val authToken: String = "",
    val isSaving: Boolean = false,
    val error: UiText? = null,
)
