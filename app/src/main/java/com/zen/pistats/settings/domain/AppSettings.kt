package com.zen.pistats.settings.domain

data class AppSettings(
    val baseUrl: String = "",
    val authToken: String = "",
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && authToken.isNotBlank()
}
