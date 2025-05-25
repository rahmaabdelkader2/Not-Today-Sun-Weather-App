package com.example.not_today_sun.notification.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.notification.view.AlarmReceiver
import com.example.not_today_sun.notification.view.DismissAlarmReceiver
import com.example.not_today_sun.notification.view.NotificationReceiver
import android.util.Log

class AlarmHelper(private val context: Context) {
    companion object {
        private const val TAG = "AlarmDebug"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarm(alarm: Alarm) {

        if (!alarm.alarmEnabled) {
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            Log.d("++++++++++++++++++============+++++++++++",alarm.id.toString())
            putExtra("ALARM_ID", alarm.id)
            putExtra("FROM_TIME", alarm.fromTimeMillis)
            putExtra("TO_TIME", alarm.toTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.fromTimeMillis,
                pendingIntent
            )
            Log.d(TAG, "Alarm set for ID: ${alarm.id} at ${alarm.fromTimeMillis}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to set alarm: ${e.message}", e)
        }
    }

    fun setNotification(alarm: Alarm) {
        Log.d(TAG, "setNotification called for alarm ID: ${alarm.id}, notificationEnabled: ${alarm.notificationEnabled}")
        if (!alarm.notificationEnabled) {
            Log.d(TAG, "Notification not enabled, skipping")
            return
        }

        val now = System.currentTimeMillis()
        val triggerTime = if (alarm.fromTimeMillis < now) now else alarm.fromTimeMillis
        Log.d(TAG, "Adjusted notification trigger time: $triggerTime")

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt() + 4000, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Notification set for ID: ${alarm.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to set notification: ${e.message}", e)
        }
    }

    fun cancelAlarm(alarm: Alarm) {
        Log.d(TAG, "cancelAlarm called for alarm ID: ${alarm.id}")

        // Cancel main alarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        // Cancel stop alarm
        val stopIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt() + 1000,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(stopPendingIntent)

        // Cancel any notification
        cancelNotification(alarm)

        // Stop ringtone if playing
        DismissAlarmReceiver.currentRingtone?.let {
            if (it.isPlaying) {
                it.stop()
                Log.d(TAG, "Ringtone stopped for alarm ID: ${alarm.id}")
            }
            DismissAlarmReceiver.currentRingtone = null
        }

        Log.d(TAG, "All alarm components canceled for ID: ${alarm.id}")
    }

    fun cancelNotification(alarm: Alarm) {
        Log.d(TAG, "cancelNotification called for alarm ID: ${alarm.id}")
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt() + 4000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Notification canceled for ID: ${alarm.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling notification: ${e.message}", e)
        }
    }
}