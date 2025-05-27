package com.example.not_today_sun.notification.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.notification.view.AlarmReceiver

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALERT
            putExtra("ALARM_ID", alarm.id)
            putExtra("TO_TIME", alarm.toTimeMillis)
            putExtra("ALARM_ENABLED", alarm.alarmEnabled)
            putExtra("NOTIFICATION_ENABLED", alarm.notificationEnabled)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, alarm.fromTimeMillis, pendingIntent
        )
    }

    fun cancelAlarm(alarm: Alarm) {
        // Cancel trigger intent
        val triggerIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALERT
        }
        val triggerPendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.toInt(), triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(triggerPendingIntent)

        // Cancel stop intent (for alarm alerts)
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS_ALERT
            putExtra("ALARM_ID", alarm.id)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.toInt() + 1000, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(stopPendingIntent)

        // Cancel dismiss intent
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS_ALERT
            putExtra("ALARM_ID", alarm.id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.toInt() + 2000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dismissPendingIntent)

        // Stop ringtone if playing
        AlarmReceiver.currentRingtone?.let { ringtone ->
            if (ringtone.isPlaying) {
                ringtone.stop()
            }
            AlarmReceiver.currentRingtone = null
        }
    }
}