package com.example.not_today_sun.model.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class RemoteDataSource{

    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en",
        count: Int? = null
    ): Result<HourlyForecastResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.weatherApiService.getHourlyForecast(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language,
                count = count
            )

            handleApiResponse(response)
        } catch (e: IOException) {
            Result.failure(NetworkException("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ApiException("Unexpected error: ${e.message}"))
        }
    }


    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<CurrentWeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.weatherApiService.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = units,
                language = language
            )

            handleApiResponse(response)
        } catch (e: IOException) {
            Result.failure(NetworkException("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ApiException("Unexpected error: ${e.message}"))
        }
    }

    private fun <T> handleApiResponse(response: retrofit2.Response<T>): Result<T> {
        return when {
            response.isSuccessful -> {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(ApiException("Empty response body"))
            }
            response.code() == 401 -> Result.failure(UnauthorizedException("Invalid API key"))
            response.code() == 429 -> Result.failure(RateLimitException("API rate limit exceeded"))
            else -> Result.failure(ApiException("API error: HTTP ${response.code()}"))
        }
    }
}

// Custom exceptions for better error handling
sealed class WeatherException(message: String) : Exception(message)
class NetworkException(message: String) : WeatherException(message)
class ApiException(message: String) : WeatherException(message)
class UnauthorizedException(message: String) : WeatherException(message)
class RateLimitException(message: String) : WeatherException(message)