package com.example.not_today_sun.model.local


import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse

interface ILocalDataSource {
    // Hourly weather operations
    suspend fun saveHourlyForecast(response: HourlyForecastResponse)
    suspend fun getHourlyForecast(): HourlyForecastResponse?
    suspend fun deleteHourlyForecast()

    // Current weather operations
    suspend fun saveCurrentWeather(response: CurrentWeatherResponse)
    suspend fun getCurrentWeather(): CurrentWeatherResponse?
    suspend fun deleteCurrentWeather()

    // Favorite locations operations
    suspend fun insertFavoriteLocation(location: FavoriteLocation)
    suspend fun getAllFavoriteLocations(): List<FavoriteLocation>
    suspend fun deleteFavoriteLocation(cityName: String)
    suspend fun getAlarmById(id: Long): Alarm?

    // Alarm operations
    suspend fun saveAlarm(alarm: Alarm): Long
    suspend fun getAllAlarms(): List<Alarm>
    suspend fun deleteAlarm(id: Long)
}