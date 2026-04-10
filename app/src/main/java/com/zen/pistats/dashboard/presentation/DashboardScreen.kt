package com.zen.pistats.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zen.pistats.R
import com.zen.pistats.core.presentation.ObserveLifecycle
import com.zen.pistats.core.presentation.UiText
import com.zen.pistats.core.presentation.asString
import com.zen.pistats.ui.theme.Cyan200
import com.zen.pistats.ui.theme.Cyan400
import com.zen.pistats.ui.theme.Danger
import com.zen.pistats.ui.theme.Ink950
import com.zen.pistats.ui.theme.Mint300
import com.zen.pistats.ui.theme.PiStatsTheme
import com.zen.pistats.ui.theme.Slate700
import com.zen.pistats.ui.theme.Slate900
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Ink950,
                        Slate900,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    title = {
                        Column {
                            Text(
                                text = "PISTATS",
                                style = MaterialTheme.typography.labelLarge,
                                color = Mint300,
                            )
                            Text(
                                text = "Tailnet Console",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                    actions = {
                        ConsoleActionButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = "Refresh now",
                            onClick = { onAction(DashboardAction.OnManualRefreshClick) },
                        )
                        ConsoleActionButton(
                            icon = Icons.Default.Settings,
                            contentDescription = "Open settings",
                            onClick = onOpenSettings,
                        )
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        OverviewHero(state = state)
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

                        item { SectionTitle("Core Signals") }
                        item {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                MetricCard(
                                    title = stringResourceSafe(R.string.cpu_title),
                                    value = stats.cpuPercent,
                                    subtitle = "Compute pressure",
                                    icon = Icons.Outlined.DeveloperBoard,
                                )
                                MetricCard(
                                    title = stringResourceSafe(R.string.memory_title),
                                    value = stats.memoryUsage,
                                    subtitle = "Working set",
                                    icon = Icons.Outlined.Memory,
                                )
                                MetricCard(
                                    title = stringResourceSafe(R.string.disk_title),
                                    value = stats.diskUsage,
                                    subtitle = "Root filesystem",
                                    icon = Icons.Outlined.Storage,
                                )
                                MetricCard(
                                    title = stringResourceSafe(R.string.temperature_title),
                                    value = stats.temperature,
                                    subtitle = "Thermal zone",
                                    icon = Icons.Outlined.Thermostat,
                                )
                                MetricCard(
                                    title = stringResourceSafe(R.string.uptime_title),
                                    value = stats.uptime,
                                    subtitle = "Since last boot",
                                    icon = Icons.Outlined.Timer,
                                )
                                MetricCard(
                                    title = stringResourceSafe(R.string.load_title),
                                    value = stats.loadAverage,
                                    subtitle = "1m / 5m / 15m",
                                    icon = Icons.Outlined.Lan,
                                )
                            }
                        }

                        item { SectionTitle("Storage + Services") }
                        item {
                            BackupCard(
                                summary = stats.backupSummary,
                                detail = stats.backupDetail,
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
                        color = Mint300,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsoleActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun OverviewHero(state: DashboardState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(28.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Cyan400.copy(alpha = 0.28f),
                            Slate700.copy(alpha = 0.72f),
                            Slate900.copy(alpha = 0.92f),
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (state.isConfigured) "TAILSCALE LOCKED" else "TAILSCALE TARGET NEEDED",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cyan200,
                )
                Text(
                    text = state.stats?.host ?: "Pi endpoint offline",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )
                Text(
                    text = when {
                        state.hostLabel.isNotBlank() -> state.hostLabel
                        else -> "Add your Pi Tailscale URL to start polling."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
                if (state.isRefreshing || state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(99.dp)),
                        color = Mint300,
                        trackColor = Color.White.copy(alpha = 0.15f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill(
                        label = if (state.isConfigured) "ACTIVE ROUTE" else "WAITING",
                        color = if (state.isConfigured) Mint300 else Warning,
                    )
                    HeroPill(
                        label = if (state.stats != null) "LIVE METRICS" else "IDLE",
                        color = if (state.stats != null) Cyan400 else Color.White.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPill(
    label: String,
    color: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
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
            containerColor = Color.White.copy(alpha = 0.04f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (isConfigured) "Telemetry stream" else "Waiting for configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = lastUpdated?.let { "Last sync $it" } ?: "Auto-refresh cadence: every 5 seconds in-app",
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
    subtitle: String,
    icon: ImageVector,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Cyan400.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Mint300,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f),
        ),
    ) {
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f),
        ),
    ) {
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
                color = when (service.status.lowercase()) {
                    "up", "running" -> Success
                    "starting", "restarting" -> Warning
                    "down", "dead", "failed" -> Danger
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
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
