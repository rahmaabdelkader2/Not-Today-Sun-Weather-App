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
        language: String = "en",
        isNetworkAvailable: Boolean = true
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
                if (isNetworkAvailable) {
                    // Fetch from internet
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
                        saveCurrentWeatherToLocal(weatherResponse)
                        _errorMessage.value = null
                    } else {
                        throw currentResult.exceptionOrNull() ?: Exception("Unknown error")
                    }

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
                        throw forecastResult.exceptionOrNull() ?: Exception("Unknown error")
                    }
                } else {
                    // Fallback to local data
                    val savedWeather = repository.getSavedCurrentWeather()
                    if (savedWeather.isSuccess) {
                        _currentWeather.value = savedWeather.getOrThrow()
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "No internet and no cached weather data"
                    }

                    val savedForecast = repository.getSavedHourlyForecast()
                    if (savedForecast.isSuccess) {
                        _hourlyForecast.value = savedForecast.getOrThrow()
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = _errorMessage.value ?: "No internet and no cached forecast data"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("HomeViewModel", "Error fetching data", e)
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
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val timeZone = TimeZone.getDefault()
            timeFormat.timeZone = timeZone
            timeFormat.format(Date(timestamp * 1000))
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error formatting time", e)
            "--"
        }
    }
}