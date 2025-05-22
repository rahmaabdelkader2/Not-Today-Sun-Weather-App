package com.example.not_today_sun.model.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    companion object {
        const val DEFAULT_UNITS = "metric"
        const val DEFAULT_LANGUAGE = "en"
    }

    @GET("forecast/hourly")
    suspend fun getHourlyForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = DEFAULT_UNITS,
        @Query("lang") language: String = DEFAULT_LANGUAGE,
        @Query("cnt") count: Int? = null
    ): Response<HourlyForecastResponse>


    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = DEFAULT_UNITS,
        @Query("lang") language: String = DEFAULT_LANGUAGE
    ): Response<CurrentWeatherResponse>
}