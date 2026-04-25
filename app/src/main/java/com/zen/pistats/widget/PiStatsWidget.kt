package com.zen.pistats.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.width
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
            .background(ColorProvider(Color(0xFF101D25)))
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text(
            text = "PiStats",
            style = TextStyle(
                color = ColorProvider(Color(0xFFD3F4E4)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tailnet monitor",
            style = TextStyle(
                color = ColorProvider(Color(0xFFC2CAD0)),
            ),
        )
        Spacer(modifier = GlanceModifier.height(10.dp))

        when {
            !isConfigured -> {
                Text(
                    text = "Configure PiStats in the app",
                    style = TextStyle(color = ColorProvider(Color(0xFFE0E3E7))),
                )
            }

            error != null -> {
                Text(
                    text = error,
                    style = TextStyle(color = ColorProvider(Color(0xFFFFB4AB))),
                )
            }

            else -> {
                WidgetStrip(
                    title = "HOST",
                    value = prefs[WidgetPreferences.HOST].orEmpty(),
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                WidgetKeyValue("CPU", prefs[WidgetPreferences.CPU].orEmpty())
                WidgetKeyValue("MEM", prefs[WidgetPreferences.MEMORY].orEmpty())
                WidgetKeyValue("DISK", prefs[WidgetPreferences.DISK].orEmpty())
                WidgetKeyValue("TEMP", prefs[WidgetPreferences.TEMPERATURE].orEmpty())
                WidgetKeyValue("UPTIME", prefs[WidgetPreferences.UPTIME].orEmpty())
                WidgetKeyValue("LOAD", prefs[WidgetPreferences.LOAD].orEmpty())
                WidgetKeyValue("BACKUP", prefs[WidgetPreferences.BACKUP].orEmpty())
                Spacer(modifier = GlanceModifier.height(10.dp))
                Text(
                    text = "Services",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFB7DCE8)),
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = prefs[WidgetPreferences.SERVICES].orEmpty(),
                    style = TextStyle(color = ColorProvider(Color(0xFFE0E3E7))),
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = "Updated ${prefs[WidgetPreferences.LAST_UPDATED].orEmpty()}",
                    style = TextStyle(color = ColorProvider(Color(0xFFC2CAD0))),
                )
            }
        }
    }
}

@Composable
private fun WidgetStrip(
    title: String,
    value: String,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color(0xFF234B5C)))
            .padding(10.dp),
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(Color(0xFFD3F4E4)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color(0xFFFFFFFF)),
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun WidgetKeyValue(label: String, value: String) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(Color(0xFFB7DCE8)),
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = value,
            style = TextStyle(color = ColorProvider(Color(0xFFE0E3E7))),
        )
    }
}

class PiStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PiStatsWidget()
}
