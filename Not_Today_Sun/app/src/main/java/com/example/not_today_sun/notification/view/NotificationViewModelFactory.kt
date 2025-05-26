package com.example.not_today_sun.notification.view

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.not_today_sun.model.repo.WeatherRepository
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import com.example.not_today_sun.notification.viewmodel.NotificationViewModel
import com.example.not_today_sun.settings.viewmodel.SettingsViewModel

class NotificationViewModelFactory(
    private val repository: WeatherRepository,
    private val alarmHelper: AlarmHelper,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository, alarmHelper,sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun create(
            context: Context,
            repository: WeatherRepository,
            alarmHelper: AlarmHelper
        ): NotificationViewModelFactory {
            return NotificationViewModelFactory(
                repository,
                alarmHelper,
                context.getSharedPreferences(
                    "WeatherSettings",
                    android.content.Context.MODE_PRIVATE
                )
            )
        }
    }}
