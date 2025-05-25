package com.example.not_today_sun.settings.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private static final String TAG = "SettingsViewModel";
    private static final String PREFS_NAME = "WeatherSettings";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_WIND_SPEED = "wind_speed";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public void saveSettings(Context context, String location, String temperature,
                             String language, String windSpeed, boolean notifications) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Log.d(TAG, "Saving settings to SharedPreferences:");
        Log.d(TAG, "Location: " + location);
        Log.d(TAG, "Temperature: " + temperature);
        Log.d(TAG, "Language: " + language);
        Log.d(TAG, "Wind Speed: " + windSpeed);
        Log.d(TAG, "Notifications: " + notifications);

        editor.putString(KEY_LOCATION, location);
        editor.putString(KEY_TEMPERATURE, temperature);
        editor.putString(KEY_LANGUAGE, language);
        editor.putString(KEY_WIND_SPEED, windSpeed);
        editor.putBoolean(KEY_NOTIFICATIONS, notifications);
        editor.apply();

        Log.d(TAG, "Settings successfully saved to SharedPreferences");
    }

    public String getLocationSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = sharedPref.getString(KEY_LOCATION, "gps");
        Log.d(TAG, "Getting location setting: " + value);
        return value;
    }

    public String getTemperatureSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = sharedPref.getString(KEY_TEMPERATURE, "celsius");
        Log.d(TAG, "Getting temperature setting: " + value);
        return value;
    }

    public String getLanguageSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = sharedPref.getString(KEY_LANGUAGE, "english");
        Log.d(TAG, "Getting language setting: " + value);
        return value;
    }

    public String getWindSpeedSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = sharedPref.getString(KEY_WIND_SPEED, "meter_sec");
        Log.d(TAG, "Getting wind speed setting: " + value);
        return value;
    }

    public boolean getNotificationsSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean value = sharedPref.getBoolean(KEY_NOTIFICATIONS, true);
        Log.d(TAG, "Getting notifications setting: " + value);
        return value;
    }
}