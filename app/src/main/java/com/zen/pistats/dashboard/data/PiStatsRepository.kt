package com.zen.pistats.dashboard.data

import com.zen.pistats.core.domain.DataError
import com.zen.pistats.core.domain.Result
import com.zen.pistats.dashboard.domain.PiStats
import com.zen.pistats.settings.domain.AppSettings

interface PiStatsRepository {
    suspend fun fetchStats(settings: AppSettings): Result<PiStats, DataError.Network>
    suspend fun wakePc(settings: AppSettings): Result<Unit, DataError.Network>
}
