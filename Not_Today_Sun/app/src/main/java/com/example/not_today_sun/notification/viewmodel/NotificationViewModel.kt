package com.example.not_today_sun.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: WeatherRepository) : ViewModel() {

    fun saveAlarm(
        dateMillis: Long,
        fromTimeMillis: Long,
        toTimeMillis: Long,
        alarmEnabled: Boolean,
        notificationEnabled: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val alarm = Alarm(
                    dateMillis = dateMillis,
                    fromTimeMillis = fromTimeMillis,
                    toTimeMillis = toTimeMillis,
                    alarmEnabled = alarmEnabled,
                    notificationEnabled = notificationEnabled
                )
                repository.saveAlarm(alarm)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun getAllAlarms(
        onSuccess: (List<Alarm>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val alarms = repository.getAllAlarms()
                onSuccess(alarms)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteAlarm(
        alarm: Alarm,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(alarm)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}