package com.example.not_today_sun.model.repo

import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.remote.RemoteDataSource

class WeatherRepository (private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource) {

    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource
        ): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository(remoteDataSource, localDataSource).also {
                    instance = it
                }
            }
        }
    }

    // Remote operations
    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en",
        count: Int? = null
    ): Result<HourlyForecastResponse> {
        return try {
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                return Result.failure(IllegalArgumentException("Invalid coordinates"))
            }
            if (apiKey.isBlank()) {
                return Result.failure(IllegalArgumentException("API key cannot be empty"))
            }
            remoteDataSource.getHourlyForecast(latitude, longitude, apiKey, units, language, count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<CurrentWeatherResponse> {
        return try {
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                return Result.failure(IllegalArgumentException("Invalid coordinates"))
            }
            if (apiKey.isBlank()) {
                return Result.failure(IllegalArgumentException("API key cannot be empty"))
            }
            remoteDataSource.getCurrentWeather(latitude, longitude, apiKey, units, language)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local operations for Hourly Forecast
    suspend fun saveHourlyForecastToLocal(response: HourlyForecastResponse) {
        return localDataSource.saveHourlyForecast(response)

    }



    // Local operations for Current Weather
    suspend fun saveCurrentWeatherToLocal(response: CurrentWeatherResponse) {
        return localDataSource.saveCurrentWeather(response)
    }



    suspend fun saveFavoriteLocation(location: FavoriteLocation) {
        localDataSource.insertFavoriteLocation(location)
    }

    suspend fun getAllFavoriteLocations(): List<FavoriteLocation> {
        return localDataSource.getAllFavoriteLocations()
    }

    suspend fun deleteFavoriteLocation(location: FavoriteLocation) {
        localDataSource.deleteFavoriteLocation(location.cityName)
    }

    suspend fun saveAlarm(alarm: Alarm): Long {
        return localDataSource.saveAlarm(alarm)
    }

    suspend fun getAllAlarms(): List<Alarm> {
        return localDataSource.getAllAlarms()

    }

    suspend fun deleteAlarm(alarmId: Long) {
        return localDataSource.deleteAlarm(alarmId)
    }
    suspend fun getAlarmById(id: Long): Alarm? {
        return localDataSource.getAlarmById(id)
    }
}
