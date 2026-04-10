package com.zen.pistats.dashboard.presentation

import androidx.compose.runtime.Stable
import com.zen.pistats.core.presentation.UiText
import com.zen.pistats.dashboard.domain.PiStats
import com.zen.pistats.dashboard.domain.ServiceStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

sealed interface DashboardAction {
    data object OnScreenStarted : DashboardAction
    data object OnScreenStopped : DashboardAction
    data object OnManualRefreshClick : DashboardAction
}

@Stable
data class DashboardState(
    val isConfigured: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hostLabel: String = "",
    val stats: PiStatsUi? = null,
    val error: UiText? = null,
)

data class PiStatsUi(
    val host: String,
    val cpuPercent: String,
    val memoryUsage: String,
    val diskUsage: String,
    val temperature: String,
    val uptime: String,
    val loadAverage: String,
    val backupSummary: String,
    val backupDetail: String,
    val lastUpdated: String,
    val services: List<ServiceStatusUi>,
)

data class ServiceStatusUi(
    val name: String,
    val status: String,
    val detail: String,
)

fun PiStats.toUi(): PiStatsUi {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    val updatedAt = runCatching { formatter.format(Instant.parse(generatedAt)) }.getOrElse { generatedAt }

    return PiStatsUi(
        host = host,
        cpuPercent = "${cpuPercent.roundToInt()}%",
        memoryUsage = "${memory.usedMb} / ${memory.totalMb} MB",
        diskUsage = "${disk.rootUsedGb} / ${disk.rootTotalGb} GB (${disk.rootUsedPercent.roundToInt()}%)",
        temperature = temperatureC?.let { "${it}°C" } ?: "Unknown",
        uptime = uptimeSeconds.toReadableDuration(),
        loadAverage = loadAverage.joinToString(" / ") { "%.2f".format(it) },
        backupSummary = when {
            backupDrive.connected && backupDrive.mounted -> "Connected and mounted"
            backupDrive.connected -> "Connected, not mounted"
            else -> "Not connected"
        },
        backupDetail = listOfNotNull(
            backupDrive.label,
            backupDrive.device,
            backupDrive.mountpoint,
        ).joinToString(" • ").ifBlank { "No backup drive metadata detected" },
        lastUpdated = updatedAt,
        services = services.map(ServiceStatus::toUi),
    )
}

private fun ServiceStatus.toUi(): ServiceStatusUi {
    return ServiceStatusUi(
        name = name.replaceFirstChar(Char::uppercase),
        status = status.replaceFirstChar(Char::uppercase),
        detail = detail,
    )
}

private fun Long.toReadableDuration(): String {
    val totalSeconds = this
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        append("${minutes}m")
    }.trim()
}
