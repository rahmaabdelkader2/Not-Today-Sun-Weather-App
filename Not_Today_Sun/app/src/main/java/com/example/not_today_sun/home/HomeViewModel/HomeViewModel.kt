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
        _isLoading.value = true
        viewModelScope.launch {
            // Fetch current weather
            Log.d("HomeViewModel", "Fetching current weather for lat: $latitude, lon: $longitude")
            val currentResult = repository.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            currentResult.onSuccess { weatherResponse ->
                Log.d("HomeViewModel", "Current weather fetched: $weatherResponse")
                _currentWeather.value = weatherResponse
                _errorMessage.value = null
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error fetching current weather: ${exception.message}", exception)
                _errorMessage.value = exception.message ?: "Failed to fetch current weather data"
            }

            // Fetch hourly forecast
            Log.d("HomeViewModel", "Fetching hourly forecast for lat: $latitude, lon: $longitude")
            val forecastResult = repository.getHourlyForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            forecastResult.onSuccess { forecastResponse ->
                Log.d("HomeViewModel", "Hourly forecast fetched: $forecastResponse")
                _hourlyForecast.value = forecastResponse
                _errorMessage.value = null
            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error fetching hourly forecast: ${exception.message}", exception)
                _errorMessage.value = exception.message ?: "Failed to fetch hourly forecast data"
            }

            _isLoading.value = false
        }
    }



    fun formatHourlyTime(timestamp: Long, timezone: Long): String {
        val timeFormat = SimpleDateFormat("h a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val offsetMillis = timezone * 1000
        calendar.timeInMillis = timestamp * 1000 + offsetMillis
        return timeFormat.format(calendar.time)
    }


}