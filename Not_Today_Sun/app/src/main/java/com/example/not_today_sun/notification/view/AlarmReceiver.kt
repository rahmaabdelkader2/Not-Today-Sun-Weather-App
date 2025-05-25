package com.example.not_today_sun.notification.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.not_today_sun.R
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmDebug"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver.onReceive called")
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val fromTime = intent.getLongExtra("FROM_TIME", 0)
        val toTime = intent.getLongExtra("TO_TIME", 0)
        val interval = intent.getLongExtra("INTERVAL", 5 * 60 * 1000) // Default 5 minutes
        Log.d(TAG, "Received alarm: ID=$alarmId, fromTime=$fromTime, toTime=$toTime, interval=$interval")

        // 1. Play alarm sound
        playAlarmSound(context)

        // 2. Show notification with dismiss action
        val hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Notification permission granted: $hasPermission")
        if (hasPermission) {
            showNotification(context, alarmId)
        } else {
            Log.w(TAG, "Notification permission not granted, skipping notification")
        }

        // 3. Schedule stop alarm at toTime
        scheduleStopAlarm(context, alarmId, toTime)

        // 4. Schedule next alarm if within time range
        val now = System.currentTimeMillis()
        val nextTrigger = now + interval
        Log.d(TAG, "Current time: $now, Next trigger: $nextTrigger, To time: $toTime")

        if (nextTrigger <= toTime) {
            Log.d(TAG, "Scheduling next alarm at $nextTrigger")
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtras(intent) // Carry forward all extras
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pendingIntent
                )
                Log.d(TAG, "Next alarm scheduled successfully for $nextTrigger")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule next alarm: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "Next trigger exceeds toTime, stopping alarm")
        }
    }

    private fun playAlarmSound(context: Context) {
        Log.d(TAG, "Attempting to play alarm sound")
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmSound)
            if (ringtone != null) {
                // Stop any existing ringtone
                DismissAlarmReceiver.currentRingtone?.let {
                    if (it.isPlaying) {
                        it.stop()
                        Log.d(TAG, "Previous ringtone stopped")
                    }
                }
                DismissAlarmReceiver.currentRingtone = ringtone
                ringtone.play()
                Log.d(TAG, "Alarm sound playing")
            } else {
                Log.w(TAG, "Ringtone is null, cannot play alarm sound")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound: ${e.message}", e)
        }
    }

    private fun scheduleStopAlarm(context: Context, alarmId: Long, toTime: Long) {
        Log.d(TAG, "Scheduling stop alarm for ID: $alarmId at $toTime")
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val stopIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt() + 1000, // Unique request code to avoid collision
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                toTime,
                pendingIntent
            )
            Log.d(TAG, "Stop alarm scheduled for ID: $alarmId at $toTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule stop alarm: ${e.message}", e)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, alarmId: Long) {
        Log.d(TAG, "Showing notification for alarm ID: $alarmId")
        try {
            val dismissIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt() + 2000, // Unique request code
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm Triggered")
                .setContentText("Time range alarm is active!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Allows swipe-to-dismiss
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)

            NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
            Log.d(TAG, "Notified with ID: ${alarmId.toInt()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }
}