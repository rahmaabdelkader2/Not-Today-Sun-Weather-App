package com.example.not_today_sun.home.HomeViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _currentWeather = MutableLiveData<CurrentWeatherResponse>()
    val currentWeather: LiveData<CurrentWeatherResponse> get() = _currentWeather

    private val _hourlyForecast = MutableLiveData<HourlyForecastResponse>()
    val hourlyForecast: LiveData<HourlyForecastResponse> get() = _hourlyForecast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchWeatherData(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ) {
        // Validate coordinates first
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            _errorMessage.value = "Invalid location coordinates"
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Fetch current weather
                val currentResult = repository.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = apiKey,
                    units = units,
                    language = language
                )
                if (currentResult.isSuccess) {
                    val weatherResponse = currentResult.getOrThrow()
                    _currentWeather.value = weatherResponse
                    _errorMessage.value = null
                } else {
                    val exception = currentResult.exceptionOrNull() ?: Exception("Unknown error")
                    _errorMessage.value = "Failed to fetch current weather: ${exception.message}"
                    Log.e("HomeViewModel", "Current weather fetch failed", exception)
                }

                // Fetch hourly forecast
                val forecastResult = repository.getHourlyForecast(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = apiKey,
                    units = units,
                    language = language
                )
                if (forecastResult.isSuccess) {
                    val forecastResponse = forecastResult.getOrThrow()
                    _hourlyForecast.value = forecastResponse
                    saveHourlyForecastToLocal(forecastResponse)
                    _errorMessage.value = null
                } else {
                    val exception = forecastResult.exceptionOrNull() ?: Exception("Unknown error")
                    _errorMessage.value = "Failed to fetch forecast: ${exception.message}"
                    Log.e("HomeViewModel", "Hourly forecast fetch failed", exception)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
                Log.e("HomeViewModel", "Network error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveHourlyForecastToLocal(forecast: HourlyForecastResponse) {
        viewModelScope.launch {
            try {
                repository.saveHourlyForecastToLocal(forecast)
                Log.d("HomeViewModel", "Hourly forecast saved to local storage")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to save hourly forecast", e)
                _errorMessage.postValue("Failed to save forecast locally")
            }
        }
    }

    fun saveCurrentWeatherToLocal(weather: CurrentWeatherResponse) {
        viewModelScope.launch {
            try {
                repository.saveCurrentWeatherToLocal(weather)
                Log.d("HomeViewModel", "Current weather saved to local storage")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to save current weather", e)
                _errorMessage.postValue("Failed to save current weather locally")
            }
        }
    }

    fun formatHourlyTime(timestamp: Long, timezoneOffset: Long): String {
        return try {
            val timeFormat = SimpleDateFormat("h a", Locale.getDefault())
            // Set the timezone to the location's timezone
            timeFormat.timeZone = TimeZone.getTimeZone("GMT")
            val adjustedTime = timestamp * 1000 + timezoneOffset * 1000
            timeFormat.format(Date(adjustedTime))
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error formatting time", e)
            "--"
        }
    }
    }
