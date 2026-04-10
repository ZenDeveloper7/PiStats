package com.zen.pistats.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zen.pistats.core.presentation.asString
import com.zen.pistats.ui.theme.Cyan400
import com.zen.pistats.ui.theme.Ink950
import com.zen.pistats.ui.theme.Mint300
import com.zen.pistats.ui.theme.PiStatsTheme
import com.zen.pistats.ui.theme.Slate700
import com.zen.pistats.ui.theme.Slate900
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoot(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.Saved -> onNavigateBack()
            }
        }
    }

    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Ink950, Slate900, MaterialTheme.colorScheme.background),
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
                                text = "TAILSCALE ROUTE",
                                style = MaterialTheme.typography.labelLarge,
                                color = Mint300,
                            )
                            Text(
                                text = "Connection Settings",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(28.dp),
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Cyan400.copy(alpha = 0.22f),
                                        Slate700.copy(alpha = 0.62f),
                                        Slate900.copy(alpha = 0.9f),
                                    ),
                                ),
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Your phone only talks to the Pi over Tailscale. Use a 100.x.x.x tailnet address or a MagicDNS .ts.net host here.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Text(
                            text = "The saved route also drives the widget refresh target.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Endpoint",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        OutlinedTextField(
                            value = state.baseUrl,
                            onValueChange = {
                                onAction(SettingsAction.OnBaseUrlChanged(it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Pi Tailscale URL") },
                            placeholder = { Text(text = "http://100.x.y.z:8787") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lan,
                                    contentDescription = null,
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        )

                        OutlinedTextField(
                            value = state.authToken,
                            onValueChange = {
                                onAction(SettingsAction.OnAuthTokenChanged(it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Bearer token") },
                            placeholder = { Text(text = "Generated by the Pi backend installer") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                )
                            },
                        )

                        if (state.error != null) {
                            Text(
                                text = state.error.asString(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Button(
                            onClick = { onAction(SettingsAction.OnSaveClick) },
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = if (state.isSaving) "Saving route..." else "Save Tailscale Route")
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.04f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Accepted inputs",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Tailscale IPs in 100.64.0.0/10 and MagicDNS hosts ending in .ts.net are accepted.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PiStatsTheme {
        SettingsScreen(
            state = SettingsState(
                baseUrl = "http://100.88.0.10:8787",
                authToken = "example-token",
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
