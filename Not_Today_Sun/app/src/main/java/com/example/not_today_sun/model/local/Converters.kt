package com.example.not_today_sun.model.local

import androidx.room.TypeConverter
import com.example.not_today_sun.model.pojo.*
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherList(value: List<Weather>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherList(value: String): List<Weather> {
        val listType = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromWeatherDataList(value: List<WeatherData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherDataList(value: String): List<WeatherData> {
        val listType = object : TypeToken<List<WeatherData>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCity(value: City): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCity(value: String): City {
        return gson.fromJson(value, City::class.java)
    }



}