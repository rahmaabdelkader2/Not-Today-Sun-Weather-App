package com.example.not_today_sun.model.remote


interface IRemoteDataSource {
    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en",
        count: Int? = null
    ): Result<HourlyForecastResponse>

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String = "metric",
        language: String = "en"
    ): Result<CurrentWeatherResponse>


}

