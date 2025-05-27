package com.example.not_today_sun.fakedata

import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import io.mockk.coEvery
import io.mockk.mockk

class MockLocalDataSource {
    private val favoriteLocations = mutableListOf<FavoriteLocation>()
    private val alarms = mutableListOf<Alarm>()
    private var hourlyForecast: HourlyForecastResponse? = null
    private var currentWeather: CurrentWeatherResponse? = null

    val instance: LocalDataSource = mockk<LocalDataSource>().apply {
        coEvery { saveHourlyForecast(any()) } answers {
            hourlyForecast = firstArg()
        }
        coEvery { getHourlyForecast() } answers {
            hourlyForecast
        }
        coEvery { deleteHourlyForecast() } answers {
            hourlyForecast = null
        }
        coEvery { saveCurrentWeather(any()) } answers {
            currentWeather = firstArg()
        }
        coEvery { getCurrentWeather() } answers {
            currentWeather
        }
        coEvery { deleteCurrentWeather() } answers {
            currentWeather = null
        }
        coEvery { insertFavoriteLocation(any<FavoriteLocation>()) } answers {
            favoriteLocations.add(firstArg())
        }
        coEvery { getAllFavoriteLocations() } answers {
            favoriteLocations.toList()
        }
        coEvery { deleteFavoriteLocation(any()) } answers {
            favoriteLocations.removeIf { it.cityName == firstArg() }
        }
        coEvery { getAlarmById(any()) } answers {
            alarms.find { it.id == firstArg() }
        }
        coEvery { saveAlarm(any()) } answers {
            val alarm = firstArg<Alarm>()
            val newId = (alarms.maxOfOrNull { it.id } ?: 0) + 1
            alarms.add(alarm.copy(id = newId))
            newId
        }
        coEvery { getAllAlarms() } answers {
            alarms.toList()
        }
        coEvery { deleteAlarm(any()) } answers {
            alarms.removeIf { it.id == firstArg() }
        }
    }

    // Helper to simulate WeatherRepository constructing FavoriteLocation from cityName
    fun addFavoriteLocationFromCityName(cityName: String) {
        val location = FavoriteLocation(
            cityName = cityName,
            latitude = 44.34, // Default values for testing
            longitude = 10.99,
            maxTemp = 297.87,
            minTemp = 296.76
        )
        favoriteLocations.add(location)
    }
}