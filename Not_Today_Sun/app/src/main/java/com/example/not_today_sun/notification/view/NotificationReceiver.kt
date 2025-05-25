package com.example.not_today_sun.notification.view

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.not_today_sun.R
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmDebug"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "NotificationReceiver.onReceive called")
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        Log.d(TAG, "Received notification for ID: $alarmId")

        // Show notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Notification permission granted: $hasPermission")
            if (hasPermission) {
                showNotification(context, alarmId)
            } else {
                Log.w(TAG, "Notification permission not granted, skipping notification")
            }
        } else {
            Log.d(TAG, "Pre-TIRAMISU, showing notification")
            showNotification(context, alarmId)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, alarmId: Long) {
        Log.d(TAG, "Showing notification for alarm ID: $alarmId")
        try {
            // Create delete intent
            val deleteIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt() + 5000,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Weather Notification")
                .setContentText("Your weather alert is active!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDeleteIntent(deletePendingIntent) // This handles swipe-to-dismiss

            NotificationManagerCompat.from(context).notify(alarmId.toInt() + 3000, builder.build())
            Log.d(TAG, "Notified with ID: ${alarmId.toInt() + 3000}")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }
}