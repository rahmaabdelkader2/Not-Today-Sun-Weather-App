package com.example.not_today_sun.model.remote

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.not_today_sun.model.local.Converters
import com.example.not_today_sun.model.pojo.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Entity(tableName = "current_weather")
@TypeConverters(Converters::class)
@Parcelize
data class CurrentWeatherResponse(
    @PrimaryKey(autoGenerate = true) val dbId: Int = 0,
    @Embedded @SerializedName("coord") val coordinates: Coordinates,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("base") val base: String,
    @Embedded @SerializedName("main") val main: CurrentMain,
    @SerializedName("visibility") val visibility: Int,
    @Embedded @SerializedName("wind") val wind: CurrentWind,
    @Embedded @SerializedName("clouds") val clouds: CurrentClouds,
//    @Embedded @SerializedName("rain") val rain: CurrentRain,
//    @Embedded @SerializedName("snow") val snow: CurrentSnow,
    @SerializedName("dt") val dateTime: Long,
    @Embedded @SerializedName("sys") val sys: CurrentSys,
    @SerializedName("timezone") val timezone: Long,
    @SerializedName("id") val cityId: Long,
    @SerializedName("name") val cityName: String,
    @SerializedName("cod") val statusCode: Int
) : Parcelable