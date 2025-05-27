package com.example.not_today_sun.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val maxTemp: Double?,
    val minTemp: Double?
)