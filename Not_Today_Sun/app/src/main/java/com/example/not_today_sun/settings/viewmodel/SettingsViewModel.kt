package com.example.not_today_sun.settings.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.Manifest

class SettingsViewModel : ViewModel() {
    companion object {
        private const val PREFS_NAME = "WeatherSettings"
        private const val KEY_LOCATION = "use_gps"
        private const val KEY_MAP = "use_map"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_MAP_LAT = "map_lat"
        private const val KEY_MAP_LON = "map_lon"
        private const val KEY_MAP_LOCATION_NAME = "map_location_name"
    }

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> get() = _notificationEnabled

    init {
        _notificationEnabled.value = false // Initialize, will be updated in getNotificationPreference
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Get current location preference (GPS or Map)
    fun getLocationPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_LOCATION, true)
    }

    // Get current map usage preference
    fun getMapPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_MAP, false)
    }

    // Get notification preference
    fun getNotificationPreference(context: Context): Boolean {
        val enabled = getSharedPreferences(context).getBoolean(KEY_NOTIFICATIONS, false)
        _notificationEnabled.postValue(enabled)
        return enabled
    }

    // Save location preference (GPS or Map)
    fun saveLocationPreference(context: Context, useGps: Boolean, useMap: Boolean) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(KEY_LOCATION, useGps)
            putBoolean(KEY_MAP, useMap)
            if (!useMap) {
                remove(KEY_MAP_LAT)
                remove(KEY_MAP_LON)
                remove(KEY_MAP_LOCATION_NAME)
            }
            apply()
        }
    }

    // Save map location
    fun saveMapLocation(context: Context, latitude: Float, longitude: Float, locationName: String) {
        with(getSharedPreferences(context).edit()) {
            putFloat(KEY_MAP_LAT, latitude)
            putFloat(KEY_MAP_LON, longitude)
            putString(KEY_MAP_LOCATION_NAME, locationName)
            putBoolean(KEY_LOCATION, false)
            putBoolean(KEY_MAP, true)
            apply()
        }
    }

    // Save notification preference
    fun saveNotificationPreference(context: Context, enabled: Boolean) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(KEY_NOTIFICATIONS, enabled)
            apply()
        }
        _notificationEnabled.postValue(enabled)
    }

    // Get saved map location
    fun getMapLocation(context: Context): Triple<Float?, Float?, String?> {
        val prefs = getSharedPreferences(context)
        return Triple(
            if (prefs.contains(KEY_MAP_LAT)) prefs.getFloat(KEY_MAP_LAT, 0f) else null,
            if (prefs.contains(KEY_MAP_LON)) prefs.getFloat(KEY_MAP_LON, 0f) else null,
            prefs.getString(KEY_MAP_LOCATION_NAME, null)
        )
    }
}