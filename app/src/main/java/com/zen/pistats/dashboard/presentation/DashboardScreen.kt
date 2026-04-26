package com.zen.pistats.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zen.pistats.R
import com.zen.pistats.core.presentation.ObserveLifecycle
import com.zen.pistats.core.presentation.UiText
import com.zen.pistats.core.presentation.asString
import com.zen.pistats.ui.theme.Danger
import com.zen.pistats.ui.theme.PiStatsTheme
import com.zen.pistats.ui.theme.Success
import com.zen.pistats.ui.theme.Warning
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardRoot(
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveLifecycle(
        onStart = { viewModel.onAction(DashboardAction.OnScreenStarted) },
        onStop = { viewModel.onAction(DashboardAction.OnScreenStopped) },
    )

    DashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                title = {
                    Column {
                        Text(
                            text = "PiStats",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Raspberry Pi monitor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(DashboardAction.OnManualRefreshClick) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh now",
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    OverviewCard(state = state)
                }

                item {
                    TelemetryCard(
                        isConfigured = state.isConfigured,
                        lastUpdated = state.stats?.lastUpdated,
                        error = state.error,
                    )
                }

                item {
                    WakePcCard(
                        isConfigured = state.isConfigured,
                        status = state.wakePcStatus,
                        error = state.wakePcError,
                        onWakeClick = { onAction(DashboardAction.OnWakePcClick) },
                    )
                }

                if (!state.isConfigured) {
                    item {
                        EmptyConfigCard()
                    }
                } else if (state.stats != null) {
                    val stats = state.stats

                    item { SectionTitle("Signals") }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 2,
                        ) {
                            MetricCard(
                                title = stringResourceSafe(R.string.cpu_title),
                                value = stats.cpuPercent,
                                subtitle = "Compute pressure",
                                progress = progressFromPercent(stats.cpuPercent),
                                icon = Icons.Outlined.DeveloperBoard,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                            MetricCard(
                                title = stringResourceSafe(R.string.memory_title),
                                value = stats.memoryUsage,
                                subtitle = "Working set",
                                progress = progressFromUsage(stats.memoryUsage),
                                icon = Icons.Outlined.Memory,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                            MetricCard(
                                title = stringResourceSafe(R.string.disk_title),
                                value = stats.diskUsage,
                                subtitle = "Root filesystem",
                                progress = progressFromDisk(stats.diskUsage),
                                icon = Icons.Outlined.Storage,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                            MetricCard(
                                title = stringResourceSafe(R.string.temperature_title),
                                value = stats.temperature,
                                subtitle = "Thermal zone",
                                progress = progressFromTemperature(stats.temperature),
                                icon = Icons.Outlined.Thermostat,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                            CompactInfoCard(
                                title = stringResourceSafe(R.string.uptime_title),
                                value = stats.uptime,
                                icon = Icons.Outlined.Timer,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                            CompactInfoCard(
                                title = stringResourceSafe(R.string.load_title),
                                value = stats.loadAverage,
                                icon = Icons.Outlined.Lan,
                                modifier = Modifier.fillMaxWidth(0.48f),
                            )
                        }
                    }

                    item { SectionTitle("Storage") }
                    item {
                        BackupCard(
                            summary = stats.backupSummary,
                            detail = stats.backupDetail,
                        )
                    }

                    item { SectionTitle("Services") }
                    items(items = stats.services, key = { it.name }) { service ->
                        ServiceCard(service = service)
                    }
                }
            }

            if (state.isLoading && state.stats == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun WakePcCard(
    isConfigured: Boolean,
    status: WakePcStatus,
    error: UiText?,
    onWakeClick: () -> Unit,
) {
    ElevatedTonalCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Wake-on-LAN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = when (status) {
                        WakePcStatus.Idle -> "Relay a magic packet through the Pi to wake your PC."
                        WakePcStatus.Waking -> "Sending wake packet..."
                        WakePcStatus.Success -> "Wake packet sent. The PC should start if Wake-on-LAN is enabled."
                        WakePcStatus.Failed -> error?.asString() ?: "Wake request failed."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Button(
                    onClick = onWakeClick,
                    enabled = isConfigured && status != WakePcStatus.Waking,
                ) {
                    Text(
                        text = if (status == WakePcStatus.Waking) {
                            "Waking..."
                        } else {
                            "Wake PC"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(state: DashboardState) {
    ElevatedTonalCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state.stats?.host ?: "No Pi connected",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.hostLabel.ifBlank { "Add a Tailscale endpoint to start polling." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lan,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            if (state.isRefreshing || state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(
                    label = if (state.isConfigured) "Configured" else "Setup needed",
                    tone = if (state.isConfigured) StatusTone.Success else StatusTone.Warning,
                )
                StatusChip(
                    label = if (state.stats != null && state.error == null) "Live metrics" else "Idle",
                    tone = if (state.stats != null && state.error == null) StatusTone.Success else StatusTone.Neutral,
                )
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    isConfigured: Boolean,
    lastUpdated: String?,
    error: UiText?,
) {
    val hasError = error != null
    ElevatedTonalCard(
        containerColor = if (hasError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = when {
                    hasError -> "Connection issue"
                    isConfigured -> "Telemetry stream"
                    else -> "Waiting for configuration"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = error?.asString()
                    ?: lastUpdated?.let { "Last sync $it. The app refreshes every 5 seconds while open." }
                    ?: "Configure a tailnet route to begin refresh polling.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun EmptyConfigCard() {
    ElevatedTonalCard(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResourceSafe(R.string.config_missing_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResourceSafe(R.string.config_missing_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricHeader(title = title, subtitle = subtitle, icon = icon)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            progress?.let {
                LinearProgressIndicator(
                    progress = { it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CompactInfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricHeader(title = title, subtitle = "Current reading", icon = icon)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetricHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp),
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BackupCard(
    summary: String,
    detail: String,
) {
    ElevatedTonalCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storage,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResourceSafe(R.string.backup_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceStatusUi) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatusChip(
                label = service.status,
                tone = service.status.toStatusTone(),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = service.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ElevatedTonalCard(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    tone: StatusTone,
) {
    val color = when (tone) {
        StatusTone.Success -> Success
        StatusTone.Warning -> Warning
        StatusTone.Error -> Danger
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        leadingIcon = {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.extraSmall,
                color = color,
                content = {},
            )
        },
    )
}

private enum class StatusTone {
    Success,
    Warning,
    Error,
    Neutral,
}

private fun String.toStatusTone(): StatusTone {
    return when (lowercase()) {
        "up", "running", "healthy" -> StatusTone.Success
        "starting", "restarting" -> StatusTone.Warning
        "down", "dead", "failed" -> StatusTone.Error
        else -> StatusTone.Neutral
    }
}

private fun progressFromPercent(value: String): Float? {
    return value.removeSuffix("%").toFloatOrNull()?.toProgress()
}

private fun progressFromUsage(value: String): Float? {
    val used = value.substringBefore("/").trim().toFloatOrNull() ?: return null
    val total = value.substringAfter("/").substringBefore("MB").trim().toFloatOrNull() ?: return null
    return if (total > 0f) (used / total).coerceIn(0f, 1f) else null
}

private fun progressFromDisk(value: String): Float? {
    return value.substringAfter("(").substringBefore("%").toFloatOrNull()?.toProgress()
}

private fun progressFromTemperature(value: String): Float? {
    return value.substringBefore("°").toFloatOrNull()?.let { (it / 85f).coerceIn(0f, 1f) }
}

private fun Float.toProgress(): Float = (this / 100f).coerceIn(0f, 1f)

@Composable
private fun stringResourceSafe(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    PiStatsTheme(dynamicColor = false) {
        DashboardScreen(
            state = DashboardState(
                isConfigured = true,
                hostLabel = "http://100.88.0.10:8787",
                stats = PiStatsUi(
                    host = "pi",
                    cpuPercent = "18%",
                    memoryUsage = "512 / 1900 MB",
                    diskUsage = "51.0 / 917.0 GB (6%)",
                    temperature = "48.2°C",
                    uptime = "1d 3h 41m",
                    loadAverage = "0.21 / 0.34 / 0.40",
                    backupSummary = "Connected, not mounted",
                    backupDetail = "PiBackup • /dev/sdb2",
                    lastUpdated = "14:32:08",
                    services = listOf(
                        ServiceStatusUi("Vaultwarden", "Up", "running"),
                        ServiceStatusUi("Trilium", "Up", "running"),
                    ),
                ),
            ),
            onAction = {},
            onOpenSettings = {},
        )
    }
}
