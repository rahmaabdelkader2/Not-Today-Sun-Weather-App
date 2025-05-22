package com.example.not_today_sun.model.pojo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize



@Parcelize
data class WeatherData(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("pop") val pop: Float,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("dt_txt") val dtTxt: String
) : Parcelable



@Parcelize
data class Main(
    @SerializedName("temp") val temp: Float,
    @SerializedName("feels_like") val feelsLike: Float,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("sea_level") val seaLevel: Int,
    @SerializedName("grnd_level") val grndLevel: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("temp_kf") val tempKf: Float
) : Parcelable

@Parcelize
data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
) : Parcelable

@Parcelize
data class Clouds(
    @SerializedName("all") val all: Long
) : Parcelable

@Parcelize
data class Wind(
    @SerializedName("speed") val speed: Float,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Float
) : Parcelable

@Parcelize
data class Sys(
    @SerializedName("pod") val pod: String
) : Parcelable

@Parcelize
data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: Coord,
    @SerializedName("country") val country: String,
    @SerializedName("population") val population: Int,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
) : Parcelable

@Parcelize
data class Coord(
    @SerializedName("lat") val lat: Float,
    @SerializedName("lon") val lon: Float
) : Parcelable