package com.example.not_today_sun.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse


@Dao
interface WeatherDao {

    // hourly forecast operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(response: HourlyForecastResponse)


    @Query("SELECT * FROM hourly_response ORDER BY id DESC LIMIT 1")
    suspend fun getHourlyForecastFourDays(): HourlyForecastResponse?


    @Query("DELETE FROM hourly_response")
    suspend fun clearAllHourlyForecasts()

    // Current weather operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(response: CurrentWeatherResponse)

    @Query("SELECT * FROM current_weather ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentWeather(): CurrentWeatherResponse?

    @Query("DELETE FROM current_weather")
    suspend fun clearAllCurrentWeather()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavoriteLocation(location: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations")
    suspend fun getAllFavoriteLocations(): List<FavoriteLocation>

    @Query("DELETE FROM favorite_locations WHERE cityName= :name")
    suspend fun deleteFavoriteLocation(name: String)

    // Alarm operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<Alarm>

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarm(id: Long)
}