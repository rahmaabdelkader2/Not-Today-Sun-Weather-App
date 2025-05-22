package com.example.not_today_sun.model.local

import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocalDataSource(private val weatherDao: WeatherDao) {

    suspend fun saveForecast(forecast: HourlyForecastResponse) = withContext(Dispatchers.IO) {
        try {
            weatherDao.insertForecast(forecast)
        } catch (e: Exception) {
            throw Exception("Failed to save forecast: ${e.message}")
        }
    }

    /**
     * Retrieves forecast for a specific city
     * @param cityId The ID of the city
     * @return The forecast data or null if not found
     */
//    suspend fun getForecast(cityId: Int): HourlyForecastResponse? = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.getForecastByCity(cityId)
//        } catch (e: Exception) {
//            null
//        }
//    }

    /**
     * Deletes forecast for a specific city
     * @param cityId The ID of the city
     */
//    suspend fun deleteForecast(cityId: Int) = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.deleteForecast(cityId)
//        } catch (e: Exception) {
//            throw Exception("Failed to delete forecast: ${e.message}")
//        }
//    }

    /**
     * Clears all forecast data from the database
     */
//    suspend fun clearForecasts() = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.clearAllForecasts()
//        } catch (e: Exception) {
//            throw Exception("Failed to clear forecasts: ${e.message}")
//        }
//    }

    /**
     * Saves current weather data to the database
     * @param weather The weather data to save
     */
//    suspend fun saveCurrentWeather(weather: CurrentWeatherResponse) = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.insertCurrentWeather(weather)
//        } catch (e: Exception) {
//            throw Exception("Failed to save current weather: ${e.message}")
//        }
//    }

    /**
     * Retrieves current weather for a specific city
     * @param cityId The ID of the city
     * @return The current weather data or null if not found
     */
    suspend fun getCurrentWeather(cityId: Int): CurrentWeatherResponse? = withContext(Dispatchers.IO) {
        try {
            weatherDao.getCurrentWeatherByCity(cityId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes current weather for a specific city
     * @param cityId The ID of the city
     */
//    suspend fun deleteCurrentWeather(cityId: Int) = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.deleteCurrentWeather(cityId)
//        } catch (e: Exception) {
//            throw Exception("Failed to delete current weather: ${e.message}")
//        }
//    }

    /**
     * Clears all current weather data from the database
     */
//    suspend fun clearCurrentWeather() = withContext(Dispatchers.IO) {
//        try {
//            weatherDao.clearAllCurrentWeather()
//        } catch (e: Exception) {
//            throw Exception("Failed to clear current weather: ${e.message}")
//        }
//    }
}