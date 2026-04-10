package com.zen.pistats.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zen.pistats.MainActivity

class PiStatsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            PiStatsWidgetContent(prefs)
        }
    }
}

@Composable
private fun PiStatsWidgetContent(prefs: Preferences) {
    val isConfigured = prefs[WidgetPreferences.IS_CONFIGURED] ?: false
    val error = prefs[WidgetPreferences.ERROR]

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFFFFFF)))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text(
            text = "PiStats",
            style = TextStyle(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        when {
            !isConfigured -> {
                Text(text = "Configure PiStats in the app")
            }

            error != null -> {
                Text(text = error)
            }

            else -> {
                MetricRow("Host", prefs[WidgetPreferences.HOST].orEmpty())
                MetricRow("CPU", prefs[WidgetPreferences.CPU].orEmpty())
                MetricRow("Memory", prefs[WidgetPreferences.MEMORY].orEmpty())
                MetricRow("Disk", prefs[WidgetPreferences.DISK].orEmpty())
                MetricRow("Temp", prefs[WidgetPreferences.TEMPERATURE].orEmpty())
                MetricRow("Uptime", prefs[WidgetPreferences.UPTIME].orEmpty())
                MetricRow("Load", prefs[WidgetPreferences.LOAD].orEmpty())
                MetricRow("Backup", prefs[WidgetPreferences.BACKUP].orEmpty())
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = "Services",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                )
                Text(text = prefs[WidgetPreferences.SERVICES].orEmpty())
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(text = "Updated ${prefs[WidgetPreferences.LAST_UPDATED].orEmpty()}")
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = TextStyle(fontWeight = FontWeight.Bold),
        )
        Text(text = value)
    }
}

class PiStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PiStatsWidget()
}
