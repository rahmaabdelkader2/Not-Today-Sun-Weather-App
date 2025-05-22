package com.example.not_today_sun.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse


@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: HourlyForecastResponse)

//    @Query("SELECT * FROM hourly_forecast WHERE city_id = :cityId LIMIT 1")
//    suspend fun getForecastByCity(cityId: Int): HourlyForecastResponse?
//
//    /**
//     * Deletes forecast for a specific city
//     * @param cityId The ID of the city
//     */
//    @Query("DELETE FROM hourly_forecast WHERE city_id = :cityId")
//    suspend fun deleteForecast(cityId: Int)

    /**
     * Clears all forecast data from the database
     */
//    @Query("DELETE FROM hourly_forecast")
//    suspend fun clearAllForecasts()
//
//    // Current Weather operations
    /**
     * Inserts or updates current weather in the database
     * @param weather The weather data to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: CurrentWeatherResponse)

    /**
     * Retrieves current weather for a specific city
     * @param cityId The ID of the city
     * @return The current weather data or null if not found
     */
    @Query("SELECT * FROM current_weather WHERE cityId = :cityId LIMIT 1")
    suspend fun getCurrentWeatherByCity(cityId: Int): CurrentWeatherResponse?

    /**
     * Deletes current weather for a specific city
     * @param cityId The ID of the city
     */
    @Query("DELETE FROM current_weather WHERE cityId = :cityId")
    suspend fun deleteCurrentWeather(cityId: Int)

    /**
     * Clears all current weather data from the database
     */
    @Query("DELETE FROM current_weather")
    suspend fun clearAllCurrentWeather()
}