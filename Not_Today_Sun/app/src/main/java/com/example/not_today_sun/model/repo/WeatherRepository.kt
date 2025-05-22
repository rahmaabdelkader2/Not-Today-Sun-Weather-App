package com.example.not_today_sun.model.repo

import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.remote.RemoteDataSource


class WeatherRepository (private val remoteDataSource: RemoteDataSource,private val localDataSource: LocalDataSource) {

    companion object {
        @Volatile
        private var instance: WeatherRepository? = null


        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource
        ): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository(remoteDataSource, localDataSource).also { instance = it }
            }
        }
    }

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


}