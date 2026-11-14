package com.example.clockwidgetstudio

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews

class ClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { render(context, manager, it) }
        scheduleMinuteRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            updateAll(context)
        }
    }

    override fun onEnabled(context: Context) {
        scheduleMinuteRefresh(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(updatePendingIntent(context))
    }

    companion object {
        private const val ACTION_UPDATE = "com.example.clockwidgetstudio.UPDATE_CLOCK"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { render(context, manager, it) }
            scheduleMinuteRefresh(context)
        }

        private fun render(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.clock_widget)
            views.setImageViewBitmap(R.id.clock_image, ClockRenderer.render(context))
            manager.updateAppWidget(id, views)
        }

        private fun updatePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockWidgetProvider::class.java).setAction(ACTION_UPDATE)
            return PendingIntent.getBroadcast(
                context, 41, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun scheduleMinuteRefresh(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 60_000L,
                60_000L,
                updatePendingIntent(context)
            )
        }
    }
}
