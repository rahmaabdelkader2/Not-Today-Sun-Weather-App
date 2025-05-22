package com.example.not_today_sun.model.pojo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coordinates(
    @SerializedName("lon") val longitude: Double,
    @SerializedName("lat") val latitude: Double
) : Parcelable

@Parcelize
data class CurrentWeather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
) : Parcelable

@Parcelize
data class CurrentMain(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int?,
    @SerializedName("grnd_level") val groundLevel: Int?
) : Parcelable

@Parcelize
data class CurrentWind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val direction: Int,
    @SerializedName("gust") val gustSpeed: Double?
) : Parcelable

@Parcelize
data class CurrentRain(
    @SerializedName("1h") val oneHourVolume: Double?
) : Parcelable

@Parcelize
data class CurrentSnow(
    @SerializedName("1h") val oneHourVolume: Double?
) : Parcelable

@Parcelize
data class CurrentClouds(
    @SerializedName("all") val cloudiness: Long
) : Parcelable

@Parcelize
data class CurrentSys(
    @SerializedName("type") val type: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
) : Parcelable