
package com.example.not_today_sun.fakedata

import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.remote.RemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk

class MockRemoteDataSource(
    private val hourlyForecasts: Map<Pair<Double, Double>, HourlyForecastResponse> = emptyMap(),
    private val currentWeathers: Map<Pair<Double, Double>, CurrentWeatherResponse> = emptyMap()
) {
    val instance: RemoteDataSource = mockk<RemoteDataSource>().apply {
        coEvery {
            getHourlyForecast(
                latitude = any(),
                longitude = any(),
                apiKey = any(),
                units = any(),
                language = any(),
                count = any()
            )
        } answers {
            val latitude = firstArg<Double>()
            val longitude = secondArg<Double>()
            hourlyForecasts[Pair(latitude, longitude)]?.let { Result.success(it) }
                ?: Result.failure(Exception("No hourly forecast for coordinates ($latitude, $longitude)"))
        }
        coEvery {
            getCurrentWeather(
                latitude = any(),
                longitude = any(),
                apiKey = any(),
                units = any(),
                language = any()
            )
        } answers {
            val latitude = firstArg<Double>()
            val longitude = secondArg<Double>()
            currentWeathers[Pair(latitude, longitude)]?.let { Result.success(it) }
                ?: Result.failure(Exception("No current weather for coordinates ($latitude, $longitude)"))
        }
    }
}