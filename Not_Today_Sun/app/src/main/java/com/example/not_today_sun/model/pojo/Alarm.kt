package com.example.not_today_sun.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long, // Date in milliseconds (midnight of the selected day)
    val fromTimeMillis: Long, // From time in milliseconds
    val toTimeMillis: Long, // To time in milliseconds
    val alarmEnabled: Boolean,
    val notificationEnabled: Boolean
)