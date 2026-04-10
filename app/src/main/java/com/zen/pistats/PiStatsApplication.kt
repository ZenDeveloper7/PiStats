package com.zen.pistats

import android.app.Application
import com.zen.pistats.app.appModule
import com.zen.pistats.widget.PiStatsWidgetSyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class PiStatsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PiStatsApplication)
            modules(appModule)
        }
        GlobalContext.get().get<PiStatsWidgetSyncManager>().ensurePeriodicRefresh()
    }
}
