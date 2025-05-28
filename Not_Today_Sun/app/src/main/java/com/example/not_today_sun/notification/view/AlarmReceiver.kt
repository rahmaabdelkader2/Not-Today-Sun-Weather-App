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
import com.example.not_today_sun.key._apiKey
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlertDebug"
        const val ACTION_TRIGGER_ALERT = "TRIGGER_ALERT"
        const val ACTION_DISMISS_ALERT = "DISMISS_ALERT"
        const val ACTION_ALARM_DISMISSED = "ALARM_DISMISSED"
        internal var currentRingtone: Ringtone? = null

        private val WEATHER_ADVICE = mapOf(
            // Group 800: Clear
            "clear sky" to "Enjoy the sunshine! Wear sunscreen if you're outside for long.",

            // Group 80x: Clouds
            "few clouds" to "Mostly clear with a few clouds. A light jacket should be enough.",
            "scattered clouds" to "Clouds are scattered. Keep an eye on the sky for changes.",
            "broken clouds" to "Expect partly cloudy skies. A versatile outfit is recommended.",
            "overcast clouds" to "Fully cloudy skies. Carry an umbrella as rain is possible.",

            // Group 2xx: Thunderstorm
            "thunderstorm with light rain" to "Light rain with thunderstorms. Stay indoors and avoid open areas.",
            "thunderstorm with rain" to "Thunderstorms with rain. Stay indoors and keep safe.",
            "thunderstorm with heavy rain" to "Heavy rain and thunderstorms. Avoid travel and stay indoors.",
            "light thunderstorm" to "Light thunderstorms possible. Stay cautious and avoid open fields.",
            "thunderstorm" to "Thunderstorms expected. Stay indoors and avoid open fields.",
            "heavy thunderstorm" to "Severe thunderstorms. Stay indoors and avoid windows.",
            "ragged thunderstorm" to "Scattered thunderstorms. Stay alert and avoid outdoor activities.",
            "thunderstorm with light drizzle" to "Light drizzle with thunderstorms. Stay indoors and be cautious.",
            "thunderstorm with drizzle" to "Drizzle with thunderstorms. Stay indoors to stay safe.",
            "thunderstorm with heavy drizzle" to "Heavy drizzle and thunderstorms. Avoid outdoor activities.",

            // Group 3xx: Drizzle
            "light intensity drizzle" to "Light drizzle expected. A light raincoat should suffice.",
            "drizzle" to "Steady drizzle. Carry a small umbrella or raincoat.",
            "heavy intensity drizzle" to "Heavy drizzle. Wear waterproof clothing and shoes.",
            "light intensity drizzle rain" to "Light drizzle with rain. A raincoat or umbrella is recommended.",
            "drizzle rain" to "Drizzle with rain. Carry an umbrella to stay dry.",
            "heavy intensity drizzle rain" to "Heavy drizzle and rain. Wear waterproof gear.",
            "shower rain and drizzle" to "Showers with drizzle. Bring an umbrella or raincoat.",
            "heavy shower rain and drizzle" to "Heavy showers with drizzle. Stay dry with waterproof clothing.",
            "shower drizzle" to "Light shower drizzle. A small umbrella should be enough.",

            // Group 5xx: Rain
            "light rain" to "Light rain expected. Carry a small umbrella or raincoat.",
            "moderate rain" to "Moderate rain. Bring an umbrella and wear waterproof shoes.",
            "heavy intensity rain" to "Heavy rain. Stay dry with an umbrella and waterproof clothing.",
            "very heavy rain" to "Very heavy rain. Avoid travel and stay indoors if possible.",
            "extreme rain" to "Extreme rain. Stay indoors and avoid flooded areas.",
            "freezing rain" to "Freezing rain. Dress warmly and watch for icy surfaces.",
            "light intensity shower rain" to "Light showers. A raincoat or umbrella is handy.",
            "shower rain" to "Showers expected. Carry an umbrella to stay dry.",
            "heavy intensity shower rain" to "Heavy showers. Wear waterproof gear and stay cautious.",
            "ragged shower rain" to "Scattered showers. Keep an umbrella ready.",

            // Group 6xx: Snow
            "light snow" to "Light snow falling. Dress warmly and watch for slippery surfaces.",
            "snow" to "Snow is falling. Wear heavy winter clothing and boots.",
            "heavy snow" to "Heavy snow. Stay warm and avoid unnecessary travel.",
            "sleet" to "Sleet expected. Dress warmly and watch for icy surfaces.",
            "light shower sleet" to "Light sleet showers. Wear waterproof and warm clothing.",
            "shower sleet" to "Sleet showers. Stay warm and cautious of slippery surfaces.",
            "light rain and snow" to "Mix of rain and snow. Wear waterproof and warm clothing.",
            "rain and snow" to "Rain and snow mix. Dress warmly and carry an umbrella.",
            "light shower snow" to "Light snow showers. Wear warm clothing and watch for ice.",
            "shower snow" to "Snow showers. Stay warm and cautious of snowy surfaces.",
            "heavy shower snow" to "Heavy snow showers. Avoid travel and dress warmly.",

            // Group 7xx: Atmosphere
            "mist" to "Misty conditions. Drive carefully and use fog lights if needed.",
            "smoke" to "Smoky conditions. Limit outdoor activity and wear a mask if needed.",
            "haze" to "Hazy conditions. Reduce outdoor exposure if air quality is poor.",
            "sand/dust whirls" to "Dust whirls possible. Protect eyes and avoid outdoor activities.",
            "fog" to "Foggy conditions. Drive slowly and use low-beam headlights.",
            "sand" to "Sandy conditions. Protect eyes and face from blowing sand.",
            "dust" to "Dusty conditions. Stay indoors to avoid dust inhalation.",
            "volcanic ash" to "Volcanic ash present. Stay indoors and seal windows.",
            "squalls" to "Sudden squalls. Secure outdoor items and stay cautious.",
            "tornado" to "Tornado warning. Seek shelter immediately in a safe location."
        )
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
                    Log.w(TAG, "Notification permission not granted")
                    return
                }

                // Get alarm from repository to access weather description
                val application = context.applicationContext as MyApplication
                val repository = application.weatherRepository
                CoroutineScope(Dispatchers.IO).launch {
                    val weatherDescription = try {
                        val alarm = repository.getAlarmById(alarmId)
                        alarm?.weatherDescription ?: run {
                            // Fallback: Fetch current weather if description not stored
                            val sharedPref = context.getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
                            val latitude = sharedPref.getFloat("map_lat", 51.5074f).toDouble()
                            val longitude = sharedPref.getFloat("map_lon", -0.1278f).toDouble()
                            val units = sharedPref.getString("temperature_unit", "metric") ?: "metric"
                            val language = sharedPref.getString("language", "en") ?: "en"
                            val weatherResult = repository.getCurrentWeather(latitude, longitude, _apiKey, units, language)
                            if (weatherResult.isSuccess) {
                                weatherResult.getOrThrow().weather.firstOrNull()?.description?.replaceFirstChar { it.uppercaseChar() } ?: "N/A"
                            } else {
                                "N/A"
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to fetch weather description: ${e.message}")
                        "N/A"
                    }

                    // Determine advice based on weather description (case-insensitive)
                    val normalizedDescription = weatherDescription.lowercase()
                    val advice = WEATHER_ADVICE[normalizedDescription] ?: "No specific advice available."
                    val notificationText = "Current weather: $weatherDescription. $advice"

                    // Create dismiss intent
                    val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_DISMISS_ALERT
                        putExtra("ALARM_ID", alarmId)
                    }
                    val dismissPendingIntent = PendingIntent.getBroadcast(
                        context, alarmId.toInt() + 2000,
                        dismissIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    // Build notification
                    val builder = NotificationCompat.Builder(context, "alarm_channel")
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(if (alarmEnabled) "Weather App Alarm" else "Weather App Notification")
                        .setContentText(notificationText)
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

                        builder.addAction(R.drawable.ic_delete, "Dismiss", dismissPendingIntent)

                        // Schedule stop alarm
                        val alarmManager = context.getSystemService(AlarmManager::class.java)
                        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                            action = ACTION_DISMISS_ALERT
                            putExtra("ALARM_ID", alarmId)
                        }
                        val stopPendingIntent = PendingIntent.getBroadcast(
                            context, alarmId.toInt() + 1000,
                            stopIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, toTime, stopPendingIntent
                        )
                        Log.d(TAG, "Scheduled stop alarm for alarmId: $alarmId at $toTime")
                    } else if (notificationEnabled) {
                        // Notification alert: Use default notification sound
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    }

                    // Post notification
                    NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
                    Log.d(TAG, "Posted notification for alarmId: $alarmId, type: ${if (alarmEnabled) "Alarm" else "Notification"}, text: $notificationText")
                }
            }

            action == ACTION_DISMISS_ALERT || (action == null && alarmId != -1L) -> {
                Log.d(TAG, "Processing dismiss for alarmId: $alarmId")
                // Stop ringtone if playing
                currentRingtone?.let { if (it.isPlaying) it.stop() }
                currentRingtone = null

                // Cancel notification
                NotificationManagerCompat.from(context).cancel(alarmId.toInt())
                Log.d(TAG, "Canceled notification for alarmId: $alarmId")

                // Delete alarm from database and cancel scheduled alarms
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