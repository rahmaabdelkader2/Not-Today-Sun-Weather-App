package com.example.not_today_sun.model.remote

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.not_today_sun.model.local.Converters
import com.example.not_today_sun.model.pojo.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Entity(tableName = "hourly_response")
@TypeConverters(Converters::class)
@Parcelize
data class HourlyForecastResponse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<WeatherData>,
    @SerializedName("city") val city: City
) : Parcelable
