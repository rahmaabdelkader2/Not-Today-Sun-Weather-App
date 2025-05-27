package com.example.not_today_sun.notification.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.not_today_sun.model.repo.WeatherRepository
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import com.example.not_today_sun.notification.viewmodel.NotificationViewModel

class NotificationViewModelFactory(
    private val repository: WeatherRepository,
    private val alarmHelper: AlarmHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository, alarmHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}