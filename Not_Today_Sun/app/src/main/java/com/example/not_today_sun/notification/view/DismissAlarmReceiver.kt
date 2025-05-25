package com.example.not_today_sun.notification.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.not_today_sun.MyApplication
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.localbroadcastmanager.content.LocalBroadcastManager // Add this import

class DismissAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmDebug"
        var currentRingtone: Ringtone? = null
        const val ACTION_ALARM_DISMISSED = "com.example.not_today_sun.ALARM_DISMISSED" // Define action
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        Log.d("DismissAlarmReceiver", "Received dismiss for alarm ID: $alarmId")
        currentRingtone?.stop()
        currentRingtone = null

        // Cancel notifications
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(alarmId.toInt())
        notificationManager.cancel(alarmId.toInt() + 3000)

        // Access repository via custom Application class
        val application = context.applicationContext as MyApplication
        val repository = application.weatherRepository
        val alarmHelper = AlarmHelper(context)

        // Delete alarm directly
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("++++++++++++++++++++++++++++", "Attempting to delete alarm with ID: $alarmId")
                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null) {
                    Log.d("++++++++++++++++++++++++++++", "Found alarm with ID: ${alarm.id}")
                    repository.deleteAlarm(alarm.id)
                    alarmHelper.cancelAlarm(alarm)
                    alarmHelper.cancelNotification(alarm)

                    // Send broadcast to notify alarm deletion
                    val broadcastIntent = Intent(ACTION_ALARM_DISMISSED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                    Log.d(TAG, "Broadcast sent: $ACTION_ALARM_DISMISSED")
                } else {
                    Log.d("++++++++++++++++++++++++++++", "Alarm not found for ID: $alarmId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting alarm: ${e.message}")
            }
        }
    }
}