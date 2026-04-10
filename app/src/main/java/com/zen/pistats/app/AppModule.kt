package com.zen.pistats.app

import com.zen.pistats.core.data.HttpClientFactory
import com.zen.pistats.dashboard.data.KtorPiStatsRepository
import com.zen.pistats.dashboard.data.PiStatsRepository
import com.zen.pistats.dashboard.presentation.DashboardViewModel
import com.zen.pistats.settings.data.AppSettingsRepository
import com.zen.pistats.settings.data.DataStoreAppSettingsRepository
import com.zen.pistats.settings.presentation.SettingsViewModel
import com.zen.pistats.widget.PiStatsWidgetSyncManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single<AppSettingsRepository> { DataStoreAppSettingsRepository(get()) }
    single<PiStatsRepository> { KtorPiStatsRepository(get()) }
    single { PiStatsWidgetSyncManager(get(), get(), get()) }

    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
