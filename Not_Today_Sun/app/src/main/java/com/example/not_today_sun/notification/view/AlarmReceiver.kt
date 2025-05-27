package com.example.not_today_sun.notification.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.not_today_sun.MyApplication
import com.example.not_today_sun.R
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlertDebug"
        const val ACTION_TRIGGER_ALERT = "com.example.weatherapp.TRIGGER_ALERT"
        const val ACTION_DISMISS_ALERT = "com.example.weatherapp.DISMISS_ALERT"
        const val ACTION_ALARM_DISMISSED = "com.example.weatherapp.ALARM_DISMISSED"
        internal var currentRingtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        var action = intent.action
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val toTime = intent.getLongExtra("TO_TIME", 0)
        val alarmEnabled = intent.getBooleanExtra("ALARM_ENABLED", false)
        val notificationEnabled = intent.getBooleanExtra("NOTIFICATION_ENABLED", false)

        when {
            action == ACTION_TRIGGER_ALERT -> {
                // Check notification permission
                val hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    return
                }

                // Create dismiss intent with explicit action
                val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_DISMISS_ALERT
                    putExtra("ALARM_ID", alarmId)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context, alarmId.toInt() + 2000,
                    dismissIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                // Build notification based on alarm type
                val builder = NotificationCompat.Builder(context, "alarm_channel")
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setDeleteIntent(dismissPendingIntent)

                if (alarmEnabled) {
                    // Alarm alert: Play ringtone and add dismiss action
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, alarmSound)
                    currentRingtone?.let { if (it.isPlaying) it.stop() }
                    currentRingtone = ringtone
                    ringtone.play()

                    builder.setContentTitle("WeatherApp Alarm")
                        .addAction(R.drawable.ic_delete, "Dismiss", dismissPendingIntent)

                    // Schedule stop alarm
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_DISMISS_ALERT
                        putExtra("ALARM_ID", alarmId)
                    }
                    val stopPendingIntent = PendingIntent.getBroadcast(
                        context, alarmId.toInt() + 1000, // Unique request code for stop
                        stopIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, toTime, stopPendingIntent
                    )
                    Log.d(TAG, "Scheduled stop alarm for alarmId: $alarmId at $toTime")
                } else if (notificationEnabled) {
                    // Notification alert: Use default notification sound
                    builder.setContentTitle("WeatherApp Notification")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }

                // Post notification
                NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
                Log.d(TAG, "Posted notification for alarmId: $alarmId, type: ${if (alarmEnabled) "Alarm" else "Notification"}")
            }

            action == ACTION_DISMISS_ALERT || (action == null && alarmId != -1L) -> {
                // Handle dismiss action or null action with valid alarmId
                Log.d(TAG, "Processing dismiss for alarmId: $alarmId")
                // Stop ringtone if playing
                currentRingtone?.let { if (it.isPlaying) it.stop() }
                currentRingtone = null

                // Cancel notification
                NotificationManagerCompat.from(context).cancel(alarmId.toInt())
                Log.d(TAG, "Canceled notification for alarmId: $alarmId")

                // Delete alert from database and cancel scheduled alarms
                val application = context.applicationContext as MyApplication
                val repository = application.weatherRepository
                val alertHelper = AlarmHelper(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val alarm = repository.getAlarmById(alarmId)
                        Log.d(TAG, "Found alarm for dismissal: $alarm (alarmId: $alarmId)")
                        if (alarm != null) {
                            repository.deleteAlarm(alarm.id)
                            alertHelper.cancelAlarm(alarm)
                            // Broadcast dismissal to update UI
                            val broadcastIntent = Intent(ACTION_ALARM_DISMISSED)
                            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                            Log.d(TAG, "Deleted alarm and broadcast dismissal for alarmId: $alarmId")
                        } else {
                            Log.w(TAG, "Alarm not found for alarmId: $alarmId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error dismissing alarm: ${e.message}")
                    }
                }
            }

            else -> {
                Log.w(TAG, "Unknown or null action received with invalid alarmId: $action, alarmId: $alarmId")
            }
        }
    }
}