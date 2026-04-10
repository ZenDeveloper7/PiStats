package com.zen.pistats.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Pi Monitor")
                        if (state.hostLabel.isNotBlank()) {
                            Text(
                                text = state.hostLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    if (state.isRefreshing || state.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50)),
                        )
                    }
                }

                item {
                    StatusBanner(
                        isConfigured = state.isConfigured,
                        lastUpdated = state.stats?.lastUpdated,
                        error = state.error,
                    )
                }

                if (!state.isConfigured) {
                    item {
                        EmptyConfigCard()
                    }
                } else if (state.stats != null) {
                    val stats = state.stats
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            MetricCard(title = stringResourceSafe(R.string.cpu_title), value = stats.cpuPercent)
                            MetricCard(title = stringResourceSafe(R.string.memory_title), value = stats.memoryUsage)
                            MetricCard(title = stringResourceSafe(R.string.disk_title), value = stats.diskUsage)
                            MetricCard(title = stringResourceSafe(R.string.temperature_title), value = stats.temperature)
                            MetricCard(title = stringResourceSafe(R.string.uptime_title), value = stats.uptime)
                            MetricCard(title = stringResourceSafe(R.string.load_title), value = stats.loadAverage)
                        }
                    }

                    item {
                        BackupCard(
                            summary = stats.backupSummary,
                            detail = stats.backupDetail,
                        )
                    }

                    item {
                        Text(
                            text = stringResourceSafe(R.string.services_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    items(items = stats.services, key = { it.name }) { service ->
                        ServiceCard(service = service)
                    }
                }
            }

            if (state.isLoading && state.stats == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(
    isConfigured: Boolean,
    lastUpdated: String?,
    error: UiText?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = if (isConfigured) "Live polling active" else "Waiting for setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = lastUpdated?.let { "Last updated $it" } ?: "Auto-refresh every 5s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (error != null) {
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun EmptyConfigCard() {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResourceSafe(R.string.config_missing_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResourceSafe(R.string.config_missing_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BackupCard(
    summary: String,
    detail: String,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResourceSafe(R.string.backup_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceStatusUi) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatusDot(status = service.status)
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
            Text(
                text = service.status,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatusDot(status: String) {
    val color = when (status.lowercase()) {
        "up", "running" -> Success
        "starting", "restarting" -> Warning
        "down", "dead", "failed" -> Danger
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Spacer(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun stringResourceSafe(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    PiStatsTheme {
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
