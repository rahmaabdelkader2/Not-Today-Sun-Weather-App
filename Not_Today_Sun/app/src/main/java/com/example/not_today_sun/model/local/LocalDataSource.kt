package com.example.not_today_sun.model.local

import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSource(private val weatherDao: WeatherDao) {
    // Hourly weather operations
    suspend fun saveHourlyForecast(response: HourlyForecastResponse) = withContext(Dispatchers.IO) {
        try {
            weatherDao.insertHourlyForecast(response)
        } catch (e: Exception) {
            throw Exception("Failed to save forecast: ${e.message}")
        }
    }

    suspend fun getHourlyForecast(): HourlyForecastResponse? = withContext(Dispatchers.IO) {
        try {
            weatherDao.getHourlyForecastFourDays()
        } catch (e: Exception) {
            throw Exception("Failed to get forecast: ${e.message}")
        }
    }

    suspend fun deleteHourlyForecast() = withContext(Dispatchers.IO) {
        try {
            weatherDao.clearAllHourlyForecasts()
        } catch (e: Exception) {
            throw Exception("Failed to delete forecast: ${e.message}")
        }
    }


    // Current weather operations
    suspend fun saveCurrentWeather(response: CurrentWeatherResponse) = withContext(Dispatchers.IO) {
        try {
            weatherDao.insertCurrentWeather(response)
        } catch (e: Exception) {
            throw Exception("Failed to save current weather: ${e.message}")
        }
    }

    suspend fun getCurrentWeather(): CurrentWeatherResponse? = withContext(Dispatchers.IO) {
        try {
            weatherDao.getCurrentWeather()
        } catch (e: Exception) {
            throw Exception("Failed to get current weather: ${e.message}")
        }
    }

    suspend fun deleteCurrentWeather() = withContext(Dispatchers.IO) {
        try {
            weatherDao.clearAllCurrentWeather()
        } catch (e: Exception) {
            throw Exception("Failed to delete current weather: ${e.message}")
        }
    }

    // Favorite locations operations
    suspend fun insertFavoriteLocation(location: FavoriteLocation) = withContext(Dispatchers.IO) {
        weatherDao.insertFavoriteLocation(location)
    }
    suspend fun getAllFavoriteLocations(): List<FavoriteLocation> = withContext(Dispatchers.IO) {
       return@withContext weatherDao.getAllFavoriteLocations()
    }
    suspend fun deleteFavoriteLocation(cityName: String) = withContext(Dispatchers.IO) {
        weatherDao.deleteFavoriteLocation(cityName)
    }

    suspend fun getAlarmById(id: Long): Alarm? = withContext(Dispatchers.IO) {
        weatherDao.getAlarmById(id)
    }


    // Alarm operations
    suspend fun saveAlarm(alarm: Alarm):Long = withContext(Dispatchers.IO) {
        return@withContext weatherDao.insertAlarm(alarm)
    }

    suspend fun getAllAlarms(): List<Alarm> = withContext(Dispatchers.IO) {
        weatherDao.getAllAlarms()
    }

    suspend fun deleteAlarm(id: Long) = withContext(Dispatchers.IO) {
        weatherDao.deleteAlarm(id)
    }
}