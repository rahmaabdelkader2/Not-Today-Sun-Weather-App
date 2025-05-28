package com.example.not_today_sun.notification.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: WeatherRepository,
    private val alarmHelper: AlarmHelper
) : ViewModel() {
    private val _alarms = MutableLiveData<List<Alarm>>()
    val alarms: LiveData<List<Alarm>> get() = _alarms

    init {
        getAllAlarms()
    }

    fun getAllAlarms() {
        viewModelScope.launch {
            val result = repository.getAllAlarms()
            _alarms.value = result
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm.id)
            alarmHelper.cancelAlarm(alarm)
            getAllAlarms()
        }
    }

    fun addAlarm(alarm: Alarm, latitude: Double, longitude: Double, apiKey: String, units: String = "metric", language: String = "en") {
        viewModelScope.launch {
            // Fetch current weather to get description
            val weatherDescription = try {
                val weatherResult = repository.getCurrentWeather(latitude, longitude, apiKey, units, language)
                if (weatherResult.isSuccess) {
                    weatherResult.getOrThrow().weather.firstOrNull()?.description?.replaceFirstChar { it.uppercaseChar() } ?: "N/A"
                } else {
                    "N/A"
                }
            } catch (e: Exception) {
                "N/A"
            }
            val alarmWithDescription = alarm.copy(weatherDescription = weatherDescription)
            val alarmId = repository.saveAlarm(alarmWithDescription)
            val savedAlarmClassifier = alarmWithDescription.copy(id = alarmId)
            alarmHelper.setAlarm(savedAlarmClassifier)
            getAllAlarms()
        }
    }
}