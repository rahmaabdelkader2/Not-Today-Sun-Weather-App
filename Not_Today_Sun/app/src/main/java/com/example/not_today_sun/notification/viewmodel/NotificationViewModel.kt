package com.example.not_today_sun.notification.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationViewModel(private val repository: WeatherRepository, private val alarmHelper: AlarmHelper,private val sharedPreferences: SharedPreferences) : ViewModel() {

    val _alarms = MutableLiveData<List<Alarm>>()
    val alarms: LiveData<List<Alarm>> get() = _alarms

    init {

        getAllAlarms()
    }
    private val TAG = "NotificationViewModel"
    fun getAllAlarms() {
        viewModelScope.launch {
            try {
                val result = repository.getAllAlarms()
                _alarms.value = result
                Log.d(TAG, "Fetched alarms: $result")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching alarms: ${e.message}")
            }
        }
    }
    fun deleteAlarm(alarm:Alarm)
    {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(alarm.id)
                Log.d(TAG, "Alarm deleted: $alarm")
                getAllAlarms()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting alarm: ${e.message}")
            }
        }
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                val alarmId = repository.saveAlarm(alarm) // Assuming it returns the ID
                val savedAlarm = alarm.copy(id = alarmId)
                alarmHelper.setAlarm(savedAlarm)
                alarmHelper.setNotification(savedAlarm)
                Log.d(TAG, "Alarm added: $alarm")
                getAllAlarms() // Refresh the list after adding
            } catch (e: Exception) {
                Log.e(TAG, "Error adding alarm: ${e.message}")
            }
        }
    }
    fun updateNotificationPreference(enabled: Boolean) {
       sharedPreferences.edit().putBoolean("KEY_NOTIFICATIONS", enabled).apply()
        Log.d(TAG, "Notification preference updated: $enabled")
    }

}
