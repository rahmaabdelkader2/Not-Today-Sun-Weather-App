package com.example.not_today_sun.model.pojo


import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.not_today_sun.model.local.Converters

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val maxTemp: Double?,
    val minTemp: Double?
)