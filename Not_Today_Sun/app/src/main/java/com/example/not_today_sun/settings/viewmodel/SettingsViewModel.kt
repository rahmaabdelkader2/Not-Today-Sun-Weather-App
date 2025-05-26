package com.example.not_today_sun.settings.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

class SettingsViewModel : ViewModel() {
    companion object {
        private const val PREFS_NAME = "WeatherSettings"
        private const val KEY_LOCATION = "use_gps"
        private const val KEY_MAP = "use_map"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_MAP_LAT = "map_lat"
        private const val KEY_MAP_LON = "map_lon"
        private const val KEY_MAP_LOCATION_NAME = "map_location_name"
        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"
        private const val KEY_LANGUAGE = "language"
    }

    private val _notificationEnabled = MutableLiveData<Boolean>()

    init {
        _notificationEnabled.value = false
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLocationPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_LOCATION, true)
    }

    fun getMapPreference(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_MAP, false)
    }

    fun getNotificationPreference(context: Context): Boolean {
        val enabled = getSharedPreferences(context).getBoolean(KEY_NOTIFICATIONS, false)
        _notificationEnabled.postValue(enabled)
        return enabled
    }

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

    fun saveNotificationPreference(context: Context, enabled: Boolean) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(KEY_NOTIFICATIONS, enabled)
            apply()
        }
        _notificationEnabled.postValue(enabled)
    }

    fun saveTemperatureUnit(context: Context, unit: String) {
        with(getSharedPreferences(context).edit()) {
            putString(KEY_TEMPERATURE_UNIT, unit)
            apply()
        }
    }

    fun getTemperatureUnit(context: Context): String {
        return getSharedPreferences(context).getString(KEY_TEMPERATURE_UNIT, "standard") ?: "standard"
    }

    fun saveWindSpeedUnit(context: Context, unit: String) {
        with(getSharedPreferences(context).edit()) {
            putString(KEY_WIND_SPEED_UNIT, unit)
            apply()
        }
    }

    fun getWindSpeedUnit(context: Context): String {
        return getSharedPreferences(context).getString(KEY_WIND_SPEED_UNIT, "m/s") ?: "m/s"
    }

    fun saveLanguage(context: Context, language: String) {
        with(getSharedPreferences(context).edit()) {
            putString(KEY_LANGUAGE, language)
            apply()
        }
        // Apply locale change
        updateLocale(context, language)
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLanguage(context: Context): String {
        return getSharedPreferences(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }
}